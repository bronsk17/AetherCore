package me.bronsk.aethercore.modules.permissions.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.sql.AESqlStorage;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupChild;
import me.bronsk.aethercore.modules.permissions.models.AEGroupSettings;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroup;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Owns table creation - a concern that belongs to neither a specific group nor a specific player.
 * Everything group-centric lives in {@link AEGroupsPermissionsSqlStorage}, everything
 * player-centric in {@link AEUsersPermissionsSqlStorage}; both are plain composed helpers (not
 * sub-storages of their own) reachable via {@link #getGroupsPermissionsSqlStorage()}/
 * {@link #getUsersPermissionsSqlStorage()}. Neither ever reaches into the other directly - when
 * group logic needs player data or vice versa, it calls back through this parent's own public
 * {@link AEPermissionsStorage} methods, the same way any other caller would.
 */
public class AEPermissionsSqlStorage extends AESqlStorage implements AEPermissionsStorage {

    private final AEGroupsPermissionsSqlStorage groupsPermissionsSqlStorage;
    private final AEUsersPermissionsSqlStorage usersPermissionsSqlStorage;

    public AEPermissionsSqlStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws SQLException {
        super(connectionSource, dialect);
        this.groupsPermissionsSqlStorage = new AEGroupsPermissionsSqlStorage(this);
        this.usersPermissionsSqlStorage = new AEUsersPermissionsSqlStorage(this);
    }

    protected AEGroupsPermissionsSqlStorage getGroupsPermissionsSqlStorage() {
        return groupsPermissionsSqlStorage;
    }

    protected AEUsersPermissionsSqlStorage getUsersPermissionsSqlStorage() {
        return usersPermissionsSqlStorage;
    }

    @Override
    public void createTables() throws SQLException {
        ConnectionSource connectionSource = getConnection();
        TableUtils.createTableIfNotExists(connectionSource, AEGroup.class);
        TableUtils.createTableIfNotExists(connectionSource, AEGroupSettings.class);
        TableUtils.createTableIfNotExists(connectionSource, AEGroupPermission.class);
        TableUtils.createTableIfNotExists(connectionSource, AEGroupChild.class);
        TableUtils.createTableIfNotExists(connectionSource, AEPlayerGroup.class);
        TableUtils.createTableIfNotExists(connectionSource, AEPlayerPermission.class);
        TableUtils.createTableIfNotExists(connectionSource, AEPlayerGroupMetadata.class);
        TableUtils.createTableIfNotExists(connectionSource, AEUserPermissionMetadata.class);
        TableUtils.createTableIfNotExists(connectionSource, AEGroupPermissionMetadata.class);

        ensureColumn(getGroupsDao(), "tcgroups", AEGroup.IS_DEFAULT_FIELD, "INT NOT NULL DEFAULT 0");
        ensureColumn(getGroupSettingsDao(), "tcgroupssettings", AEGroupSettings.GROUP_ID_FIELD, "INT NOT NULL");
        ensureColumn(getGroupSettingsDao(), "tcgroupssettings", AEGroupSettings.PREFIX_FIELD, "VARCHAR(256)");
        ensureColumn(getGroupSettingsDao(), "tcgroupssettings", AEGroupSettings.SUFFIX_FIELD, "VARCHAR(256)");
        ensureColumn(getGroupSettingsDao(), "tcgroupssettings", AEGroupSettings.PRIORITY_FIELD, "INT NOT NULL DEFAULT 0");
        ensureRestrictForeignKey(getGroupSettingsDao(), "tcgroupssettings", "fk_tcgroupssettings_group_id",
                AEGroupSettings.GROUP_ID_FIELD, "tcgroups", AEGroup.ID_FIELD);
        ensureColumn(getGroupPermissionsDao(), "tcgroupspermissions", AEGroupPermission.GROUP_ID_FIELD, "INT NOT NULL");
        ensureColumn(getGroupPermissionsDao(), "tcgroupspermissions", AEGroupPermission.PERMISSION_FIELD, "VARCHAR(255) NOT NULL DEFAULT ''");
        ensureRestrictForeignKey(getGroupPermissionsDao(), "tcgroupspermissions", "fk_tcgroupspermissions_group_id",
                AEGroupPermission.GROUP_ID_FIELD, "tcgroups", AEGroup.ID_FIELD);
        ensureColumn(getGroupChildrenDao(), "tcgroupchildren", AEGroupChild.GROUP_ID_FIELD, "INT NOT NULL");
        ensureColumn(getGroupChildrenDao(), "tcgroupchildren", AEGroupChild.CHILD_GROUP_ID_FIELD, "INT NOT NULL");
        ensureRestrictForeignKey(getGroupChildrenDao(), "tcgroupchildren", "fk_tcgroupchildren_group_id",
                AEGroupChild.GROUP_ID_FIELD, "tcgroups", AEGroup.ID_FIELD);
        ensureRestrictForeignKey(getGroupChildrenDao(), "tcgroupchildren", "fk_tcgroupchildren_child_group_id",
                AEGroupChild.CHILD_GROUP_ID_FIELD, "tcgroups", AEGroup.ID_FIELD);
        ensureColumn(getPlayerGroupsDao(), "tcplayersgroups", AEPlayerGroup.PLAYER_UUID_FIELD, "VARCHAR(255)");
        ensureColumn(getPlayerGroupsDao(), "tcplayersgroups", AEPlayerGroup.GROUP_ID_FIELD, "INT NOT NULL");
        ensureRestrictForeignKey(getPlayerGroupsDao(), "tcplayersgroups", "fk_tcplayersgroups_group_id",
                AEPlayerGroup.GROUP_ID_FIELD, "tcgroups", AEGroup.ID_FIELD);
        ensureColumn(getPlayerPermissionsDao(), "tcplayerpermissions", AEPlayerPermission.PLAYER_UUID_FIELD, "VARCHAR(255)");
        ensureColumn(getPlayerPermissionsDao(), "tcplayerpermissions", AEPlayerPermission.PERMISSION_FIELD, "VARCHAR(255) NOT NULL DEFAULT ''");
        ensureColumn(getPlayerGroupMetadataDao(), "tcplayergroupmetadata", AEPlayerGroupMetadata.RELATION_ID_FIELD, "INT NOT NULL");
        ensureColumn(getPlayerGroupMetadataDao(), "tcplayergroupmetadata", AEPlayerGroupMetadata.WORLD_NAME_FIELD, "VARCHAR(255)");
        ensureColumn(getPlayerGroupMetadataDao(), "tcplayergroupmetadata", AEPlayerGroupMetadata.EXPIRES_AT_FIELD, "TIMESTAMP");
        ensureRestrictForeignKey(getPlayerGroupMetadataDao(), "tcplayergroupmetadata", "fk_tcplayergroupmetadata_relation_id",
                AEPlayerGroupMetadata.RELATION_ID_FIELD, "tcplayersgroups", AEPlayerGroup.ID_FIELD);
        ensureColumn(getUserPermissionMetadataDao(), "tcuserpermissionmetadata", AEUserPermissionMetadata.RELATION_ID_FIELD, "INT NOT NULL");
        ensureColumn(getUserPermissionMetadataDao(), "tcuserpermissionmetadata", AEUserPermissionMetadata.WORLD_NAME_FIELD, "VARCHAR(255)");
        ensureColumn(getUserPermissionMetadataDao(), "tcuserpermissionmetadata", AEUserPermissionMetadata.EXPIRES_AT_FIELD, "TIMESTAMP");
        ensureRestrictForeignKey(getUserPermissionMetadataDao(), "tcuserpermissionmetadata", "fk_tcuserpermissionmetadata_relation_id",
                AEUserPermissionMetadata.RELATION_ID_FIELD, "tcplayerpermissions", AEPlayerPermission.ID_FIELD);
        ensureColumn(getGroupPermissionMetadataDao(), "tcgrouppermissionmetadata", AEGroupPermissionMetadata.RELATION_ID_FIELD, "INT NOT NULL");
        ensureColumn(getGroupPermissionMetadataDao(), "tcgrouppermissionmetadata", AEGroupPermissionMetadata.WORLD_NAME_FIELD, "VARCHAR(255)");
        ensureColumn(getGroupPermissionMetadataDao(), "tcgrouppermissionmetadata", AEGroupPermissionMetadata.EXPIRES_AT_FIELD, "TIMESTAMP");
        ensureRestrictForeignKey(getGroupPermissionMetadataDao(), "tcgrouppermissionmetadata", "fk_tcgrouppermissionmetadata_relation_id",
                AEGroupPermissionMetadata.RELATION_ID_FIELD, "tcgroupspermissions", AEGroupPermission.ID_FIELD);
    }

    @Override
    public void warmup() throws SQLException {
        groupsPermissionsSqlStorage.refreshGroups();
    }

    @Override
    public AEGroup getGroup(String name) throws SQLException {
        return groupsPermissionsSqlStorage.getGroup(name);
    }

    @Override
    public AEGroupSettings getGroupSettings(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupSettings(groupName);
    }

    @Override
    public AEGroupSettings getOrCreateGroupSettings(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getOrCreateGroupSettings(groupName);
    }

    @Override
    public void saveGroupSettings(String groupName, AEGroupSettings settings) throws SQLException {
        groupsPermissionsSqlStorage.saveGroupSettings(groupName, settings);
    }

    @Override
    public AEGroup getOrCreateGroup(String name) throws SQLException {
        return groupsPermissionsSqlStorage.getOrCreateGroup(name);
    }

    @Override
    public Set<String> getPlayerPermissions(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getPlayerPermissions(playerUuid);
    }

    @Override
    public Set<String> getDirectPlayerPermissions(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getDirectPlayerPermissions(playerUuid);
    }

    @Override
    public Map<String, Timestamp> getPlayerGroupCreatedAtByPlayerUuid(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getPlayerGroupCreatedAtByPlayerUuid(groupName);
    }

    @Override
    public Set<String> getGroupPermissions(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupPermissions(groupName);
    }

    @Override
    public List<AEGroupPermission> getGroupPermissionsOrderedByRecent(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupPermissionsOrderedByRecent(groupName);
    }

    @Override
    public List<AEPlayerPermission> getDirectPlayerPermissionsOrderedByRecent(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getDirectPlayerPermissionsOrderedByRecent(playerUuid);
    }

    @Override
    public Set<String> getGroupChildren(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupChildren(groupName);
    }

    @Override
    public Map<String, Timestamp> getGroupChildrenOrderedByRecent(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupChildrenOrderedByRecent(groupName);
    }

    @Override
    public Set<String> getTransitiveGroupChildren(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getTransitiveGroupChildren(groupName);
    }

    @Override
    public void addGroupChild(String groupName, String childGroupName) throws SQLException {
        groupsPermissionsSqlStorage.addGroupChild(groupName, childGroupName);
    }

    @Override
    public void removeGroupChild(String groupName, String childGroupName) throws SQLException {
        groupsPermissionsSqlStorage.removeGroupChild(groupName, childGroupName);
    }

    @Override
    public Set<String> getEffectiveGroupPermissions(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getEffectiveGroupPermissions(groupName);
    }

    @Override
    public Set<String> getPlayerGroups(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getPlayerGroups(playerUuid);
    }

    @Override
    public List<AEGroup> queryGroups(AEPageQuery query) throws SQLException {
        return groupsPermissionsSqlStorage.queryGroups(query);
    }

    @Override
    public long countGroups(AEPageQuery query) throws SQLException {
        return groupsPermissionsSqlStorage.countGroups(query);
    }

    @Override
    public Set<String> getAllRegisteredGroups() throws SQLException {
        return groupsPermissionsSqlStorage.getAllRegisteredGroups();
    }

    @Override
    public AEGroup getDefaultGroup() throws SQLException {
        return groupsPermissionsSqlStorage.getDefaultGroup();
    }

    @Override
    public void addPlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException {
        usersPermissionsSqlStorage.addPlayerPermissions(playerUuid, permissions);
    }

    @Override
    public void removePlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException {
        usersPermissionsSqlStorage.removePlayerPermissions(playerUuid, permissions);
    }

    @Override
    public void addGroupPermissions(String groupName, Set<String> permissions) throws SQLException {
        groupsPermissionsSqlStorage.addGroupPermissions(groupName, permissions);
    }

    @Override
    public void removeGroupPermissions(String groupName, Set<String> permissions) throws SQLException {
        groupsPermissionsSqlStorage.removeGroupPermissions(groupName, permissions);
    }

    @Override
    public void addPlayerToGroup(String playerUuid, String groupName) throws SQLException {
        usersPermissionsSqlStorage.addPlayerToGroup(playerUuid, groupName);
    }

    @Override
    public void removePlayerFromGroup(String playerUuid, String groupName) throws SQLException {
        usersPermissionsSqlStorage.removePlayerFromGroup(playerUuid, groupName);
    }

    @Override
    public void setPlayerGroup(String playerUuid, String groupName) throws SQLException {
        usersPermissionsSqlStorage.setPlayerGroup(playerUuid, groupName);
    }

    @Override
    public void setDefaultGroup(String groupName) throws SQLException {
        groupsPermissionsSqlStorage.setDefaultGroup(groupName);
    }

    @Override
    public void invalidatePlayerPermissions(String playerUuid) {
        usersPermissionsSqlStorage.invalidatePlayerPermissions(playerUuid);
    }

    @Override
    public void invalidatePlayerGroups(String playerUuid) {
        usersPermissionsSqlStorage.invalidatePlayerGroups(playerUuid);
    }

    @Override
    public Map<String, AEGroup> getCachedGroups() {
        return groupsPermissionsSqlStorage.getCachedGroups();
    }

    /** Every player UUID with a permissions footprint (a direct grant or a group membership) - not filtered by any specific node. */
    @Override
    public Set<String> getPermissionCandidateUuids() throws SQLException {
        Set<String> candidateUuids = new LinkedHashSet<>();
        for (AEPlayerPermission relation : getPlayerPermissionsDao().queryForAll()) {
            candidateUuids.add(relation.getPlayerUuid());
        }
        for (AEPlayerGroup relation : getPlayerGroupsDao().queryForAll()) {
            candidateUuids.add(relation.getPlayerUuid());
        }
        return Collections.unmodifiableSet(candidateUuids);
    }

    /** Every distinct permission string currently granted to some group or some player - used to suggest known nodes when granting a new one. */
    @Override
    public Set<String> getAllKnownPermissions() throws SQLException {
        Set<String> known = new LinkedHashSet<>();
        for (AEGroupPermission relation : getGroupPermissionsDao().queryForAll()) {
            known.add(relation.getPermission());
        }
        for (AEPlayerPermission relation : getPlayerPermissionsDao().queryForAll()) {
            known.add(relation.getPermission());
        }
        return known;
    }

    @Override
    public AEPlayerGroupMetadata getPlayerGroupMetadata(String playerUuid, String groupName) throws SQLException {
        return usersPermissionsSqlStorage.getPlayerGroupMetadata(playerUuid, groupName);
    }

    @Override
    public boolean savePlayerGroupMetadata(String playerUuid, String groupName, String worldName, Timestamp expiresAt) throws SQLException {
        return usersPermissionsSqlStorage.savePlayerGroupMetadata(playerUuid, groupName, worldName, expiresAt);
    }

    @Override
    public AEUserPermissionMetadata getPlayerPermissionMetadata(String playerUuid, String permission) throws SQLException {
        return usersPermissionsSqlStorage.getPlayerPermissionMetadata(playerUuid, permission);
    }

    @Override
    public boolean savePlayerPermissionMetadata(String playerUuid, String permission, String worldName, Timestamp expiresAt) throws SQLException {
        return usersPermissionsSqlStorage.savePlayerPermissionMetadata(playerUuid, permission, worldName, expiresAt);
    }

    @Override
    public AEGroupPermissionMetadata getGroupPermissionMetadata(String groupName, String permission) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupPermissionMetadata(groupName, permission);
    }

    @Override
    public boolean saveGroupPermissionMetadata(String groupName, String permission, String worldName, Timestamp expiresAt) throws SQLException {
        return groupsPermissionsSqlStorage.saveGroupPermissionMetadata(groupName, permission, worldName, expiresAt);
    }

    @Override
    public Map<String, AEUserPermissionMetadata> getPlayerPermissionMetadataByNode(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getPlayerPermissionMetadataByNode(playerUuid);
    }

    @Override
    public Map<String, AEGroupPermissionMetadata> getGroupPermissionMetadataByNode(String groupName) throws SQLException {
        return groupsPermissionsSqlStorage.getGroupPermissionMetadataByNode(groupName);
    }

    @Override
    public int deleteExpiredPlayerPermissions() throws SQLException {
        return usersPermissionsSqlStorage.deleteExpiredPlayerPermissions();
    }

    @Override
    public int deleteExpiredPlayerGroups() throws SQLException {
        return usersPermissionsSqlStorage.deleteExpiredPlayerGroups();
    }

    @Override
    public int deleteExpiredGroupPermissions() throws SQLException {
        return groupsPermissionsSqlStorage.deleteExpiredGroupPermissions();
    }

    @Override
    public Map<String, Timestamp> getGroupCreatedAtByGroupName(String playerUuid) throws SQLException {
        return usersPermissionsSqlStorage.getGroupCreatedAtByGroupName(playerUuid);
    }

    @Override
    public void deleteGroup(String groupName) throws SQLException {
        groupsPermissionsSqlStorage.deleteGroup(groupName);
    }

    @Override
    public void clearCache() {
        groupsPermissionsSqlStorage.clearCache();
        usersPermissionsSqlStorage.clearCache();
    }

    protected Dao<AEGroup, Integer> getGroupsDao() throws SQLException {
        return getDao(AEGroup.class);
    }

    protected Dao<AEGroupSettings, Integer> getGroupSettingsDao() throws SQLException {
        return getDao(AEGroupSettings.class);
    }

    protected Dao<AEPlayerPermission, Integer> getPlayerPermissionsDao() throws SQLException {
        return getDao(AEPlayerPermission.class);
    }

    protected Dao<AEPlayerGroup, Integer> getPlayerGroupsDao() throws SQLException {
        return getDao(AEPlayerGroup.class);
    }

    protected Dao<AEGroupPermission, Integer> getGroupPermissionsDao() throws SQLException {
        return getDao(AEGroupPermission.class);
    }

    protected Dao<AEGroupChild, Integer> getGroupChildrenDao() throws SQLException {
        return getDao(AEGroupChild.class);
    }

    protected Dao<AEPlayerGroupMetadata, Integer> getPlayerGroupMetadataDao() throws SQLException {
        return getDao(AEPlayerGroupMetadata.class);
    }

    protected Dao<AEUserPermissionMetadata, Integer> getUserPermissionMetadataDao() throws SQLException {
        return getDao(AEUserPermissionMetadata.class);
    }

    protected Dao<AEGroupPermissionMetadata, Integer> getGroupPermissionMetadataDao() throws SQLException {
        return getDao(AEGroupPermissionMetadata.class);
    }
}
