package me.bronsk.aethercore.modules.permissions.storage;

import com.j256.ormlite.stmt.DeleteBuilder;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupSettings;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroup;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Every player-centric SQL operation: direct grants and group membership, plus the two player-
 * keyed caches (both moved here since they exist solely to back these methods). Reaches back into
 * the owning {@link AEPermissionsSqlStorage} for shared DAOs, timestamps and anything group-related
 * it needs (falling back to the default group, resolving a group's own permission set) - never
 * touches {@link AEGroupsPermissionsSqlStorage} directly.
 */
public class AEUsersPermissionsSqlStorage {

    private final AEPermissionsSqlStorage sqlStorage;
    private final Map<String, Set<String>> playerGroupsByUuid = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> playerPermissionsByUuid = new ConcurrentHashMap<>();

    public AEUsersPermissionsSqlStorage(AEPermissionsSqlStorage sqlStorage) {
        this.sqlStorage = sqlStorage;
    }

    public Set<String> getPlayerPermissions(String playerUuid) throws SQLException {
        Set<String> cached = playerPermissionsByUuid.get(playerUuid);
        if (cached != null) {
            return cached;
        }

        Set<String> permissions = new LinkedHashSet<>(getDirectPlayerPermissions(playerUuid));
        for (AEGroup group : getPlayerGroupModels(playerUuid)) {
            permissions.addAll(sqlStorage.getEffectiveGroupPermissions(group.getName()));
        }

        Set<String> immutablePermissions = Collections.unmodifiableSet(permissions);
        playerPermissionsByUuid.put(playerUuid, immutablePermissions);
        return immutablePermissions;
    }

    public Set<String> getDirectPlayerPermissions(String playerUuid) throws SQLException {
        Set<String> permissions = new LinkedHashSet<>();
        List<AEPlayerPermission> relations = sqlStorage.getPlayerPermissionsDao().queryBuilder()
                .where()
                .eq(AEPlayerPermission.PLAYER_UUID_FIELD, playerUuid)
                .query();
        for (AEPlayerPermission relation : relations) {
            permissions.add(relation.getPermission());
        }
        return Collections.unmodifiableSet(permissions);
    }

    /** Every direct-grant metadata row for this player that actually has metadata set, keyed by permission node - one query, not one per node; grants without metadata (the overwhelming majority) simply don't appear. */
    public Map<String, AEUserPermissionMetadata> getPlayerPermissionMetadataByNode(String playerUuid) throws SQLException {
        List<AEPlayerPermission> relations = sqlStorage.getPlayerPermissionsDao().queryBuilder()
                .where().eq(AEPlayerPermission.PLAYER_UUID_FIELD, playerUuid)
                .query();
        if (relations.isEmpty()) {
            return Map.of();
        }
        Map<Integer, String> nodeByRelationId = new HashMap<>();
        for (AEPlayerPermission relation : relations) {
            nodeByRelationId.put(relation.getId(), relation.getPermission());
        }
        List<AEUserPermissionMetadata> metadataRows = sqlStorage.getUserPermissionMetadataDao().queryBuilder()
                .where().in(AEUserPermissionMetadata.RELATION_ID_FIELD, nodeByRelationId.keySet())
                .query();
        Map<String, AEUserPermissionMetadata> byNode = new HashMap<>();
        for (AEUserPermissionMetadata metadata : metadataRows) {
            String node = nodeByRelationId.get(metadata.getRelationId());
            if (node != null) {
                byNode.put(node, metadata);
            }
        }
        return byNode;
    }

    public Set<String> getPlayerGroups(String playerUuid) throws SQLException {
        Set<String> cached = playerGroupsByUuid.get(playerUuid);
        if (cached != null) {
            return cached;
        }

        List<AEGroup> groups = getPlayerGroupModels(playerUuid);
        Map<String, Integer> priorities = getGroupPriorities(groups);
        groups.sort(Comparator.comparingInt((AEGroup group) -> priorities.getOrDefault(group.getName(), 0))
                .reversed()
                .thenComparing(AEGroup::getName));

        Set<String> groupNames = new LinkedHashSet<>();
        for (AEGroup group : groups) {
            groupNames.add(group.getName());
        }
        Set<String> immutableGroups = Collections.unmodifiableSet(groupNames);
        playerGroupsByUuid.put(playerUuid, immutableGroups);
        return immutableGroups;
    }

    /** A player's own direct permission grants, newest first - the full row (not just the node string), so callers already have {@code getCreatedAt()} without a second query per row. */
    public List<AEPlayerPermission> getDirectPlayerPermissionsOrderedByRecent(String playerUuid) throws SQLException {
        return sqlStorage.getPlayerPermissionsDao().queryBuilder()
                .orderBy("created_at", false)
                .where().eq(AEPlayerPermission.PLAYER_UUID_FIELD, playerUuid)
                .query();
    }

    public void addPlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException {
        for (String permission : permissions) {
            if (getPlayerPermissionRelation(playerUuid, permission) == null) {
                sqlStorage.getPlayerPermissionsDao().create(new AEPlayerPermission(0, playerUuid, permission, AEDateUtils.now(), AEDateUtils.now()));
            }
        }
        invalidatePlayerPermissions(playerUuid);
    }

    public void removePlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException {
        for (String permission : permissions) {
            AEPlayerPermission relation = getPlayerPermissionRelation(playerUuid, permission);
            if (relation != null) {
                deletePlayerPermissionMetadata(relation.getId());
            }
            DeleteBuilder<AEPlayerPermission, Integer> deleteBuilder = sqlStorage.getPlayerPermissionsDao().deleteBuilder();
            deleteBuilder.where()
                    .eq(AEPlayerPermission.PLAYER_UUID_FIELD, playerUuid)
                    .and()
                    .eq(AEPlayerPermission.PERMISSION_FIELD, permission);
            deleteBuilder.delete();
        }
        invalidatePlayerPermissions(playerUuid);
    }

    /** The world/expiry metadata attached to this player's grant of {@code permission}, or null if none is set. */
    public AEUserPermissionMetadata getPlayerPermissionMetadata(String playerUuid, String permission) throws SQLException {
        AEPlayerPermission relation = getPlayerPermissionRelation(playerUuid, permission);
        if (relation == null) {
            return null;
        }
        return sqlStorage.getUserPermissionMetadataDao().queryBuilder()
                .where().eq(AEUserPermissionMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
    }

    /** Creates or updates the world/expiry metadata for this player's grant of {@code permission} - false if the grant itself doesn't exist. */
    public boolean savePlayerPermissionMetadata(String playerUuid, String permission, String worldName, Timestamp expiresAt) throws SQLException {
        AEPlayerPermission relation = getPlayerPermissionRelation(playerUuid, permission);
        if (relation == null) {
            return false;
        }
        Timestamp now = AEDateUtils.now();
        AEUserPermissionMetadata existing = sqlStorage.getUserPermissionMetadataDao().queryBuilder()
                .where().eq(AEUserPermissionMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
        if (existing == null) {
            sqlStorage.getUserPermissionMetadataDao().create(new AEUserPermissionMetadata(0, relation.getId(), worldName, expiresAt, now, now));
        } else {
            existing.setWorldName(worldName);
            existing.setExpiresAt(expiresAt);
            existing.setUpdatedAt(now);
            sqlStorage.getUserPermissionMetadataDao().update(existing);
        }
        return true;
    }

    private void deletePlayerPermissionMetadata(int relationId) throws SQLException {
        DeleteBuilder<AEUserPermissionMetadata, Integer> deleteBuilder = sqlStorage.getUserPermissionMetadataDao().deleteBuilder();
        deleteBuilder.where().eq(AEUserPermissionMetadata.RELATION_ID_FIELD, relationId);
        deleteBuilder.delete();
    }

    public void addPlayerToGroup(String playerUuid, String groupName) throws SQLException {
        AEGroup group = sqlStorage.getOrCreateGroup(groupName);
        if (getPlayerGroupRelation(playerUuid, group.getId()) == null) {
            sqlStorage.getPlayerGroupsDao().create(new AEPlayerGroup(0, playerUuid, group.getId(), AEDateUtils.now(), AEDateUtils.now()));
        }
        invalidatePlayerGroups(playerUuid);
    }

    public void removePlayerFromGroup(String playerUuid, String groupName) throws SQLException {
        AEGroup group = sqlStorage.getGroup(groupName);
        if (group == null) {
            return;
        }
        AEPlayerGroup relation = getPlayerGroupRelation(playerUuid, group.getId());
        if (relation != null) {
            deletePlayerGroupMetadata(relation.getId());
        }
        DeleteBuilder<AEPlayerGroup, Integer> deleteBuilder = sqlStorage.getPlayerGroupsDao().deleteBuilder();
        deleteBuilder.where()
                .eq(AEPlayerGroup.PLAYER_UUID_FIELD, playerUuid)
                .and()
                .eq(AEPlayerGroup.GROUP_ID_FIELD, group.getId());
        deleteBuilder.delete();
        invalidatePlayerGroups(playerUuid);
    }

    /** The world/expiry metadata attached to this player's membership in {@code groupName}, or null if none is set. */
    public AEPlayerGroupMetadata getPlayerGroupMetadata(String playerUuid, String groupName) throws SQLException {
        AEGroup group = sqlStorage.getGroup(groupName);
        if (group == null) {
            return null;
        }
        AEPlayerGroup relation = getPlayerGroupRelation(playerUuid, group.getId());
        if (relation == null) {
            return null;
        }
        return sqlStorage.getPlayerGroupMetadataDao().queryBuilder()
                .where().eq(AEPlayerGroupMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
    }

    /** Creates or updates the world/expiry metadata for this player's membership in {@code groupName} - false if the membership itself doesn't exist. */
    public boolean savePlayerGroupMetadata(String playerUuid, String groupName, String worldName, Timestamp expiresAt) throws SQLException {
        AEGroup group = sqlStorage.getGroup(groupName);
        if (group == null) {
            return false;
        }
        AEPlayerGroup relation = getPlayerGroupRelation(playerUuid, group.getId());
        if (relation == null) {
            return false;
        }
        Timestamp now = AEDateUtils.now();
        AEPlayerGroupMetadata existing = sqlStorage.getPlayerGroupMetadataDao().queryBuilder()
                .where().eq(AEPlayerGroupMetadata.RELATION_ID_FIELD, relation.getId())
                .queryForFirst();
        if (existing == null) {
            sqlStorage.getPlayerGroupMetadataDao().create(new AEPlayerGroupMetadata(0, relation.getId(), worldName, expiresAt, now, now));
        } else {
            existing.setWorldName(worldName);
            existing.setExpiresAt(expiresAt);
            existing.setUpdatedAt(now);
            sqlStorage.getPlayerGroupMetadataDao().update(existing);
        }
        return true;
    }

    private void deletePlayerGroupMetadata(int relationId) throws SQLException {
        DeleteBuilder<AEPlayerGroupMetadata, Integer> deleteBuilder = sqlStorage.getPlayerGroupMetadataDao().deleteBuilder();
        deleteBuilder.where().eq(AEPlayerGroupMetadata.RELATION_ID_FIELD, relationId);
        deleteBuilder.delete();
    }

    public void setPlayerGroup(String playerUuid, String groupName) throws SQLException {
        DeleteBuilder<AEPlayerGroup, Integer> deleteBuilder = sqlStorage.getPlayerGroupsDao().deleteBuilder();
        deleteBuilder.where()
                .eq(AEPlayerGroup.PLAYER_UUID_FIELD, playerUuid);
        deleteBuilder.delete();
        addPlayerToGroup(playerUuid, groupName);
    }

    /**
     * Physically deletes every direct player-permission grant (and its metadata row) whose expiry
     * has already passed - the runtime resolver already excludes these via {@code
     * AEPermissionsUsersModule#getRuntimeUserPermissions}, so this is pure housekeeping (the rows
     * would otherwise linger forever as harmless history). The metadata row is deleted first since
     * its {@code relation_id} foreign key is RESTRICT, not CASCADE - same order {@link
     * #removePlayerPermissions} already uses.
     *
     * @return How many grants were removed.
     */
    public int deleteExpiredPlayerPermissions() throws SQLException {
        Timestamp now = AEDateUtils.now();
        List<AEUserPermissionMetadata> expired = sqlStorage.getUserPermissionMetadataDao().queryBuilder()
                .where().isNotNull(AEUserPermissionMetadata.EXPIRES_AT_FIELD)
                .and().le(AEUserPermissionMetadata.EXPIRES_AT_FIELD, now)
                .query();
        int removed = 0;
        Set<String> affectedPlayers = new HashSet<>();
        for (AEUserPermissionMetadata metadata : expired) {
            AEPlayerPermission relation = sqlStorage.getPlayerPermissionsDao().queryForId(metadata.getRelationId());
            sqlStorage.getUserPermissionMetadataDao().delete(metadata);
            if (relation != null) {
                sqlStorage.getPlayerPermissionsDao().delete(relation);
                affectedPlayers.add(relation.getPlayerUuid());
                removed++;
            }
        }
        for (String playerUuid : affectedPlayers) {
            invalidatePlayerPermissions(playerUuid);
        }
        return removed;
    }

    /**
     * Physically deletes every player-group membership (and its metadata row) whose expiry has
     * already passed - same housekeeping role as {@link #deleteExpiredPlayerPermissions}, just for
     * memberships instead of direct grants.
     *
     * @return How many memberships were removed.
     */
    public int deleteExpiredPlayerGroups() throws SQLException {
        Timestamp now = AEDateUtils.now();
        List<AEPlayerGroupMetadata> expired = sqlStorage.getPlayerGroupMetadataDao().queryBuilder()
                .where().isNotNull(AEPlayerGroupMetadata.EXPIRES_AT_FIELD)
                .and().le(AEPlayerGroupMetadata.EXPIRES_AT_FIELD, now)
                .query();
        int removed = 0;
        Set<String> affectedPlayers = new HashSet<>();
        for (AEPlayerGroupMetadata metadata : expired) {
            AEPlayerGroup relation = sqlStorage.getPlayerGroupsDao().queryForId(metadata.getRelationId());
            sqlStorage.getPlayerGroupMetadataDao().delete(metadata);
            if (relation != null) {
                sqlStorage.getPlayerGroupsDao().delete(relation);
                affectedPlayers.add(relation.getPlayerUuid());
                removed++;
            }
        }
        for (String playerUuid : affectedPlayers) {
            invalidatePlayerGroups(playerUuid);
        }
        return removed;
    }

    public void invalidatePlayerPermissions(String playerUuid) {
        playerPermissionsByUuid.remove(playerUuid);
    }

    public void invalidatePlayerGroups(String playerUuid) {
        playerGroupsByUuid.remove(playerUuid);
        invalidatePlayerPermissions(playerUuid);
    }

    /** Bulk-invalidates every player's cached permission set - used when a group-side mutation (a permission grant, a parent change) ripples out to every member. */
    void clearPlayerPermissionsCache() {
        playerPermissionsByUuid.clear();
    }

    /** Bulk-invalidates every player's cached group membership (and, transitively, their permissions) - used when group-side membership resolution changes (default group, group settings). */
    void clearPlayerGroupsCache() {
        playerGroupsByUuid.clear();
        playerPermissionsByUuid.clear();
    }

    public void clearCache() {
        playerGroupsByUuid.clear();
        playerPermissionsByUuid.clear();
    }

    private List<AEGroup> getPlayerGroupModels(String playerUuid) throws SQLException {
        List<AEPlayerGroup> relations = sqlStorage.getPlayerGroupsDao().queryBuilder()
                .where()
                .eq(AEPlayerGroup.PLAYER_UUID_FIELD, playerUuid)
                .query();

        List<AEGroup> groups = new ArrayList<>();
        for (AEPlayerGroup relation : relations) {
            AEGroup group = sqlStorage.getGroupsDao().queryForId(relation.getGroupId());
            if (group != null) {
                groups.add(group);
            }
        }
        if (groups.isEmpty()) {
            AEGroup defaultGroup = sqlStorage.getDefaultGroup();
            if (defaultGroup != null) {
                groups.add(defaultGroup);
            }
        }
        return groups;
    }

    private Map<String, Integer> getGroupPriorities(List<AEGroup> groups) throws SQLException {
        if (groups.isEmpty()) {
            return Map.of();
        }

        List<Integer> groupIds = groups.stream().map(AEGroup::getId).toList();
        Map<Integer, Integer> priorityByGroupId = new HashMap<>();
        for (AEGroupSettings settings : sqlStorage.getGroupSettingsDao().queryBuilder()
                .where().in(AEGroupSettings.GROUP_ID_FIELD, groupIds).query()) {
            priorityByGroupId.put(settings.getGroupId(), settings.getPriority());
        }

        Map<String, Integer> priorities = new HashMap<>();
        for (AEGroup group : groups) {
            priorities.put(group.getName(), priorityByGroupId.getOrDefault(group.getId(), 0));
        }
        return priorities;
    }

    /** Every group {@code playerUuid} directly belongs to, newest membership first, alongside when each membership was created - one query, no per-row follow-up. */
    public Map<String, Timestamp> getGroupCreatedAtByGroupName(String playerUuid) throws SQLException {
        Map<String, Timestamp> createdAtByGroupName = new LinkedHashMap<>();
        List<AEPlayerGroup> relations = sqlStorage.getPlayerGroupsDao().queryBuilder()
                .orderBy("created_at", false)
                .where().eq(AEPlayerGroup.PLAYER_UUID_FIELD, playerUuid)
                .query();
        for (AEPlayerGroup relation : relations) {
            AEGroup group = sqlStorage.getGroupsDao().queryForId(relation.getGroupId());
            if (group != null) {
                createdAtByGroupName.put(group.getName(), relation.getCreatedAt());
            }
        }
        return createdAtByGroupName;
    }

    private AEPlayerPermission getPlayerPermissionRelation(String playerUuid, String permission) throws SQLException {
        return sqlStorage.getPlayerPermissionsDao().queryBuilder()
                .where()
                .eq(AEPlayerPermission.PLAYER_UUID_FIELD, playerUuid)
                .and()
                .eq(AEPlayerPermission.PERMISSION_FIELD, permission)
                .queryForFirst();
    }

    private AEPlayerGroup getPlayerGroupRelation(String playerUuid, int groupId) throws SQLException {
        return sqlStorage.getPlayerGroupsDao().queryBuilder()
                .where()
                .eq(AEPlayerGroup.PLAYER_UUID_FIELD, playerUuid)
                .and()
                .eq(AEPlayerGroup.GROUP_ID_FIELD, groupId)
                .queryForFirst();
    }
}
