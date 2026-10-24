package me.bronsk.aethercore.modules.permissions.storage;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupChild;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEGroupSettings;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroup;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every group-centric SQL operation: catalog, settings, membership resolution and the group
 * inheritance DAG (a group can inherit from any number of "children"). Reaches back into the
 * owning {@link AEPermissionsSqlStorage} for shared DAOs and timestamps - never touches
 * {@link AEUsersPermissionsSqlStorage} directly, even when a mutation here needs to invalidate a
 * player-keyed cache; that always goes through the parent.
 */
public class AEGroupsPermissionsSqlStorage {

    private final AEPermissionsSqlStorage sqlStorage;
    private final Map<String, AEGroup> groups = new ConcurrentHashMap<>();

    public AEGroupsPermissionsSqlStorage(AEPermissionsSqlStorage sqlStorage) {
        this.sqlStorage = sqlStorage;
    }

    protected void refreshGroups() throws SQLException {
        groups.clear();
        for (AEGroup group : sqlStorage.getGroupsDao().queryForAll()) {
            groups.put(group.getName(), group);
        }
    }

    public AEGroup getGroup(String name) throws SQLException {
        AEGroup cached = groups.get(name);
        if (cached != null) {
            return cached;
        }

        AEGroup group = sqlStorage.getGroupsDao().queryBuilder()
                .where()
                .eq(AEGroup.NAME_FIELD, name)
                .queryForFirst();
        if (group != null) {
            groups.put(name, group);
        }
        return group;
    }

    public AEGroup getOrCreateGroup(String name) throws SQLException {
        AEGroup group = getGroup(name);
        if (group != null) {
            return group;
        }

        Timestamp now = AEDateUtils.now();
        AEGroup created = new AEGroup(0, name, now, now);
        sqlStorage.getGroupsDao().create(created);
        groups.put(name, created);
        return created;
    }

    public AEGroupSettings getGroupSettings(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return null;
        }

        return sqlStorage.getGroupSettingsDao().queryBuilder()
                .where()
                .eq(AEGroupSettings.GROUP_ID_FIELD, group.getId())
                .queryForFirst();
    }

    public AEGroupSettings getOrCreateGroupSettings(String groupName) throws SQLException {
        AEGroup group = getOrCreateGroup(groupName);
        AEGroupSettings settings = getGroupSettings(groupName);
        if (settings != null) {
            return settings;
        }

        Timestamp now = AEDateUtils.now();
        AEGroupSettings created = new AEGroupSettings(0, group.getId(), now, now);
        sqlStorage.getGroupSettingsDao().create(created);
        return created;
    }

    public void saveGroupSettings(String groupName, AEGroupSettings settings) throws SQLException {
        settings.setUpdatedAt(AEDateUtils.now());
        sqlStorage.getGroupSettingsDao().createOrUpdate(settings);
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerGroupsCache();
    }

    public List<AEGroup> queryGroups(AEPageQuery query) throws SQLException {
        QueryBuilder<AEGroup, Integer> builder = sqlStorage.getGroupsDao().queryBuilder();
        if (query.hasSearch()) {
            builder.where().like(AEGroup.NAME_FIELD, "%" + query.search() + "%");
        }
        List<AEGroup> result = builder
                .orderBy(AEGroup.NAME_FIELD, true)
                .offset(query.offset())
                .limit((long) query.pageSize())
                .query();
        for (AEGroup group : result) {
            groups.put(group.getName(), group);
        }
        return result;
    }

    public long countGroups(AEPageQuery query) throws SQLException {
        if (!query.hasSearch()) {
            return sqlStorage.getGroupsDao().countOf();
        }
        QueryBuilder<AEGroup, Integer> builder = sqlStorage.getGroupsDao().queryBuilder();
        builder.where().like(AEGroup.NAME_FIELD, "%" + query.search() + "%");
        return builder.countOf();
    }

    public Set<String> getAllRegisteredGroups() throws SQLException {
        Set<String> groupNames = new LinkedHashSet<>();
        for (AEGroup group : sqlStorage.getGroupsDao().queryForAll()) {
            groupNames.add(group.getName());
            groups.put(group.getName(), group);
        }
        return Collections.unmodifiableSet(groupNames);
    }

    public AEGroup getDefaultGroup() throws SQLException {
        AEGroup group = sqlStorage.getGroupsDao().queryBuilder()
                .where()
                .eq(AEGroup.IS_DEFAULT_FIELD, true)
                .queryForFirst();
        if (group != null) {
            groups.put(group.getName(), group);
        }
        return group;
    }

    public Set<String> getGroupPermissions(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return Set.of();
        }

        Set<String> permissions = new LinkedHashSet<>();
        List<AEGroupPermission> relations = sqlStorage.getGroupPermissionsDao().queryBuilder()
                .where()
                .eq(AEGroupPermission.GROUP_ID_FIELD, group.getId())
                .query();
        for (AEGroupPermission relation : relations) {
            permissions.add(relation.getPermission());
        }
        return Collections.unmodifiableSet(permissions);
    }

    /** Every direct-grant metadata row for this group that actually has metadata set, keyed by permission node - one query, not one per node; grants without metadata (the overwhelming majority) simply don't appear. */
    public Map<String, AEGroupPermissionMetadata> getGroupPermissionMetadataByNode(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return Map.of();
        }
        List<AEGroupPermission> relations = sqlStorage.getGroupPermissionsDao().queryBuilder()
                .where().eq(AEGroupPermission.GROUP_ID_FIELD, group.getId())
                .query();
        if (relations.isEmpty()) {
            return Map.of();
        }
        Map<Integer, String> nodeByRelationId = new HashMap<>();
        for (AEGroupPermission relation : relations) {
            nodeByRelationId.put(relation.getId(), relation.getPermission());
        }
        List<AEGroupPermissionMetadata> metadataRows = sqlStorage.getGroupPermissionMetadataDao().queryBuilder()
                .where().in(AEGroupPermissionMetadata.RELATION_ID_FIELD, nodeByRelationId.keySet())
                .query();
        Map<String, AEGroupPermissionMetadata> byNode = new HashMap<>();
        for (AEGroupPermissionMetadata metadata : metadataRows) {
            String node = nodeByRelationId.get(metadata.getRelationId());
            if (node != null) {
                byNode.put(node, metadata);
            }
        }
        return byNode;
    }

    /** A group's own direct permission grants, newest first - the full row (not just the node string), so callers already have {@code getCreatedAt()} without a second query per row. */
    public List<AEGroupPermission> getGroupPermissionsOrderedByRecent(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return List.of();
        }

        return sqlStorage.getGroupPermissionsDao().queryBuilder()
                .orderBy("created_at", false)
                .where().eq(AEGroupPermission.GROUP_ID_FIELD, group.getId())
                .query();
    }

    public void addGroupPermissions(String groupName, Set<String> permissions) throws SQLException {
        AEGroup group = getOrCreateGroup(groupName);
        for (String permission : permissions) {
            if (getGroupPermissionRelation(group.getId(), permission) == null) {
                sqlStorage.getGroupPermissionsDao().create(new AEGroupPermission(0, group.getId(), permission, AEDateUtils.now(), AEDateUtils.now()));
            }
        }
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerPermissionsCache();
    }

    public void removeGroupPermissions(String groupName, Set<String> permissions) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return;
        }

        for (String permission : permissions) {
            AEGroupPermission relation = getGroupPermissionRelation(group.getId(), permission);
            if (relation != null) {
                deleteGroupPermissionMetadata(relation.getId());
            }
            DeleteBuilder<AEGroupPermission, Integer> deleteBuilder = sqlStorage.getGroupPermissionsDao().deleteBuilder();
            deleteBuilder.where()
                    .eq(AEGroupPermission.GROUP_ID_FIELD, group.getId())
                    .and()
                    .eq(AEGroupPermission.PERMISSION_FIELD, permission);
            deleteBuilder.delete();
        }
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerPermissionsCache();
    }

    /** The world/expiry metadata attached to this group's grant of {@code permission}, or null if none is set. */
    public AEGroupPermissionMetadata getGroupPermissionMetadata(String groupName, String permission) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return null;
        }
        AEGroupPermission relation = getGroupPermissionRelation(group.getId(), permission);
        if (relation == null) {
            return null;
        }
        return sqlStorage.getGroupPermissionMetadataDao().queryBuilder()
                .where().eq(AEGroupPermissionMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
    }

    /** Creates or updates the world/expiry metadata for this group's grant of {@code permission} - false if the grant itself doesn't exist. */
    public boolean saveGroupPermissionMetadata(String groupName, String permission, String worldName, Timestamp expiresAt) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return false;
        }
        AEGroupPermission relation = getGroupPermissionRelation(group.getId(), permission);
        if (relation == null) {
            return false;
        }
        Timestamp now = AEDateUtils.now();
        AEGroupPermissionMetadata existing = sqlStorage.getGroupPermissionMetadataDao().queryBuilder()
                .where().eq(AEGroupPermissionMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
        if (existing == null) {
            sqlStorage.getGroupPermissionMetadataDao().create(new AEGroupPermissionMetadata(0, relation.getId(), worldName, expiresAt, now, now));
        } else {
            existing.setWorldName(worldName);
            existing.setExpiresAt(expiresAt);
            existing.setUpdatedAt(now);
            sqlStorage.getGroupPermissionMetadataDao().update(existing);
        }
        return true;
    }

    private void deleteGroupPermissionMetadata(int relationId) throws SQLException {
        DeleteBuilder<AEGroupPermissionMetadata, Integer> deleteBuilder = sqlStorage.getGroupPermissionMetadataDao().deleteBuilder();
        deleteBuilder.where().eq(AEGroupPermissionMetadata.RELATION_ID_FIELD, relationId);
        deleteBuilder.delete();
    }

    /**
     * Physically deletes every group-permission grant (and its metadata row) whose expiry has
     * already passed - the runtime resolver already excludes these via {@code
     * AEPermissionsGroupsModule#getRuntimeGroupPermissions}, so this is pure housekeeping (the rows
     * would otherwise linger forever as harmless history). The metadata row is deleted first since
     * its {@code relation_id} foreign key is RESTRICT, not CASCADE - same order {@link
     * #removeGroupPermissions} already uses.
     *
     * @return How many grants were removed.
     */
    public int deleteExpiredGroupPermissions() throws SQLException {
        Timestamp now = AEDateUtils.now();
        List<AEGroupPermissionMetadata> expired = sqlStorage.getGroupPermissionMetadataDao().queryBuilder()
                .where().isNotNull(AEGroupPermissionMetadata.EXPIRES_AT_FIELD)
                .and().le(AEGroupPermissionMetadata.EXPIRES_AT_FIELD, now)
                .query();
        int removed = 0;
        boolean anyRemoved = false;
        for (AEGroupPermissionMetadata metadata : expired) {
            AEGroupPermission relation = sqlStorage.getGroupPermissionsDao().queryForId(metadata.getRelationId());
            sqlStorage.getGroupPermissionMetadataDao().delete(metadata);
            if (relation != null) {
                sqlStorage.getGroupPermissionsDao().delete(relation);
                removed++;
                anyRemoved = true;
            }
        }
        if (anyRemoved) {
            sqlStorage.getUsersPermissionsSqlStorage().clearPlayerPermissionsCache();
        }
        return removed;
    }

    public Set<String> getGroupChildren(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return Set.of();
        }

        Set<String> children = new LinkedHashSet<>();
        List<AEGroupChild> relations = sqlStorage.getGroupChildrenDao().queryBuilder()
                .where()
                .eq(AEGroupChild.GROUP_ID_FIELD, group.getId())
                .query();
        for (AEGroupChild relation : relations) {
            AEGroup child = sqlStorage.getGroupsDao().queryForId(relation.getChildGroupId());
            if (child != null) {
                children.add(child.getName());
            }
        }
        return Collections.unmodifiableSet(children);
    }

    /** Groups {@code groupName} directly inherits from, newest edge first, alongside when each edge was created - one query, no per-row follow-up. */
    public Map<String, Timestamp> getGroupChildrenOrderedByRecent(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return Map.of();
        }

        Map<String, Timestamp> children = new LinkedHashMap<>();
        List<AEGroupChild> relations = sqlStorage.getGroupChildrenDao().queryBuilder()
                .orderBy("created_at", false)
                .where().eq(AEGroupChild.GROUP_ID_FIELD, group.getId())
                .query();
        for (AEGroupChild relation : relations) {
            AEGroup child = sqlStorage.getGroupsDao().queryForId(relation.getChildGroupId());
            if (child != null) {
                children.put(child.getName(), relation.getCreatedAt());
            }
        }
        return children;
    }

    public Set<String> getTransitiveGroupChildren(String groupName) throws SQLException {
        return AEPermissionsGroupResolver.getTransitiveGroupChildren(sqlStorage, groupName);
    }

    public Set<String> getEffectiveGroupPermissions(String groupName) throws SQLException {
        return AEPermissionsGroupResolver.getEffectiveGroupPermissions(sqlStorage, groupName);
    }

    public void addGroupChild(String groupName, String childGroupName) throws SQLException {
        AEGroup group = getOrCreateGroup(groupName);
        AEGroup child = getOrCreateGroup(childGroupName);
        if (group.getName().equals(child.getName())) {
            throw new IllegalArgumentException("Group cannot inherit from itself: " + groupName);
        }

        if (getTransitiveGroupChildren(child.getName()).contains(group.getName())) {
            throw new IllegalArgumentException("Group child would create a cycle: " + groupName + " -> " + childGroupName);
        }

        if (getGroupChildRelation(group.getId(), child.getId()) == null) {
            sqlStorage.getGroupChildrenDao().create(new AEGroupChild(0, group.getId(), child.getId(), AEDateUtils.now(), AEDateUtils.now()));
        }
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerPermissionsCache();
    }

    public void removeGroupChild(String groupName, String childGroupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        AEGroup child = getGroup(childGroupName);
        if (group == null || child == null) {
            return;
        }

        DeleteBuilder<AEGroupChild, Integer> deleteBuilder = sqlStorage.getGroupChildrenDao().deleteBuilder();
        deleteBuilder.where()
                .eq(AEGroupChild.GROUP_ID_FIELD, group.getId())
                .and()
                .eq(AEGroupChild.CHILD_GROUP_ID_FIELD, child.getId());
        deleteBuilder.delete();
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerPermissionsCache();
    }

    private AEGroupChild getGroupChildRelation(int groupId, int childGroupId) throws SQLException {
        return sqlStorage.getGroupChildrenDao().queryBuilder()
                .where()
                .eq(AEGroupChild.GROUP_ID_FIELD, groupId)
                .and()
                .eq(AEGroupChild.CHILD_GROUP_ID_FIELD, childGroupId)
                .queryForFirst();
    }

    public void setDefaultGroup(String groupName) throws SQLException {
        AEGroup targetGroup = getOrCreateGroup(groupName);
        Timestamp now = AEDateUtils.now();
        for (AEGroup group : sqlStorage.getGroupsDao().queryForAll()) {
            boolean defaultGroup = group.getName().equals(targetGroup.getName());
            if (group.isDefault() == defaultGroup) {
                groups.put(group.getName(), group);
                continue;
            }
            group.setDefault(defaultGroup);
            group.setUpdatedAt(now);
            sqlStorage.getGroupsDao().update(group);
            groups.put(group.getName(), group);
        }
        sqlStorage.getUsersPermissionsSqlStorage().clearPlayerGroupsCache();
    }

    /** Every player UUID directly assigned to {@code groupName}, newest membership first, alongside when each membership was created - one query, no per-row follow-up. */
    public Map<String, Timestamp> getPlayerGroupCreatedAtByPlayerUuid(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return Map.of();
        }

        Map<String, Timestamp> createdAtByUuid = new LinkedHashMap<>();
        List<AEPlayerGroup> relations = sqlStorage.getPlayerGroupsDao().queryBuilder()
                .orderBy("created_at", false)
                .where().eq(AEPlayerGroup.GROUP_ID_FIELD, group.getId())
                .query();
        for (AEPlayerGroup relation : relations) {
            createdAtByUuid.put(relation.getPlayerUuid(), relation.getCreatedAt());
        }
        return createdAtByUuid;
    }

    public Map<String, AEGroup> getCachedGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public void clearCache() {
        groups.clear();
    }

    /**
     * Cascade-deletes a group and everything that directly depends on it: its own permission
     * grants (and their metadata), its own "inherits from" edges, its settings, and every player's
     * membership in it (and that membership's metadata). Does NOT touch other groups that inherit
     * FROM this one - the caller must guard against that first (see {@code
     * AEPermissionsGroupsModule.handleDeleteGroup}), since those groups' own {@code child_group_id}
     * foreign key would otherwise be left dangling and the delete would fail at the DB level.
     */
    public void deleteGroup(String groupName) throws SQLException {
        AEGroup group = getGroup(groupName);
        if (group == null) {
            return;
        }
        int groupId = group.getId();

        for (AEGroupPermission relation : sqlStorage.getGroupPermissionsDao().queryBuilder()
                .where().eq(AEGroupPermission.GROUP_ID_FIELD, groupId).query()) {
            DeleteBuilder<AEGroupPermissionMetadata, Integer> metaDelete = sqlStorage.getGroupPermissionMetadataDao().deleteBuilder();
            metaDelete.where().eq(AEGroupPermissionMetadata.RELATION_ID_FIELD, relation.getId());
            metaDelete.delete();
        }
        DeleteBuilder<AEGroupPermission, Integer> permissionsDelete = sqlStorage.getGroupPermissionsDao().deleteBuilder();
        permissionsDelete.where().eq(AEGroupPermission.GROUP_ID_FIELD, groupId);
        permissionsDelete.delete();

        DeleteBuilder<AEGroupChild, Integer> childrenDelete = sqlStorage.getGroupChildrenDao().deleteBuilder();
        childrenDelete.where().eq(AEGroupChild.GROUP_ID_FIELD, groupId);
        childrenDelete.delete();

        for (AEPlayerGroup relation : sqlStorage.getPlayerGroupsDao().queryBuilder()
                .where().eq(AEPlayerGroup.GROUP_ID_FIELD, groupId).query()) {
            DeleteBuilder<AEPlayerGroupMetadata, Integer> metaDelete = sqlStorage.getPlayerGroupMetadataDao().deleteBuilder();
            metaDelete.where().eq(AEPlayerGroupMetadata.RELATION_ID_FIELD, relation.getId());
            metaDelete.delete();
        }
        DeleteBuilder<AEPlayerGroup, Integer> membershipsDelete = sqlStorage.getPlayerGroupsDao().deleteBuilder();
        membershipsDelete.where().eq(AEPlayerGroup.GROUP_ID_FIELD, groupId);
        membershipsDelete.delete();

        AEGroupSettings settings = getGroupSettings(groupName);
        if (settings != null) {
            sqlStorage.getGroupSettingsDao().delete(settings);
        }

        sqlStorage.getGroupsDao().delete(group);
        groups.remove(groupName);
        sqlStorage.getUsersPermissionsSqlStorage().clearCache();
    }

    private AEGroupPermission getGroupPermissionRelation(int groupId, String permission) throws SQLException {
        return sqlStorage.getGroupPermissionsDao().queryBuilder()
                .where()
                .eq(AEGroupPermission.GROUP_ID_FIELD, groupId)
                .and()
                .eq(AEGroupPermission.PERMISSION_FIELD, permission)
                .queryForFirst();
    }
}
