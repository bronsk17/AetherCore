package me.bronsk.aethercore.modules.permissions.storage;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEGroupSettings;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AEPermissionsStorage extends AutoCloseable {

    void warmup() throws SQLException;

    AEGroup getGroup(String name) throws SQLException;

    AEGroup getOrCreateGroup(String name) throws SQLException;

    List<AEGroup> queryGroups(AEPageQuery query) throws SQLException;

    long countGroups(AEPageQuery query) throws SQLException;

    AEGroupSettings getGroupSettings(String groupName) throws SQLException;

    AEGroupSettings getOrCreateGroupSettings(String groupName) throws SQLException;

    void saveGroupSettings(String groupName, AEGroupSettings settings) throws SQLException;

    /** The player's own direct permissions, not merged with anything granted via their groups. */
    Set<String> getDirectPlayerPermissions(String playerUuid) throws SQLException;

    /** Every player UUID directly assigned to this group, alongside when each membership was created - not paginated, resolved/paged by the caller. */
    Map<String, Timestamp> getPlayerGroupCreatedAtByPlayerUuid(String groupName) throws SQLException;

    Set<String> getPlayerPermissions(String playerUuid) throws SQLException;

    Set<String> getGroupPermissions(String groupName) throws SQLException;

    /** A group's own direct permission grants, newest first, as full rows (so {@code getCreatedAt()} needs no follow-up query) - for the Group Perms tab's grid. */
    List<AEGroupPermission> getGroupPermissionsOrderedByRecent(String groupName) throws SQLException;

    /** A player's own direct permission grants, newest first, as full rows (so {@code getCreatedAt()} needs no follow-up query) - for the Player Perms tab's grid. */
    List<AEPlayerPermission> getDirectPlayerPermissionsOrderedByRecent(String playerUuid) throws SQLException;

    /** Groups {@code groupName} directly inherits from - one hop, not transitive. */
    Set<String> getGroupChildren(String groupName) throws SQLException;

    /** Groups {@code groupName} directly inherits from, newest edge first, alongside when each edge was created - for the Group Children tab's grid. */
    Map<String, Timestamp> getGroupChildrenOrderedByRecent(String groupName) throws SQLException;

    /** Every group reachable from {@code groupName} by following "inherits from" edges, transitively. */
    Set<String> getTransitiveGroupChildren(String groupName) throws SQLException;

    /** Makes {@code groupName} inherit from {@code childGroupName} - throws {@link IllegalArgumentException} for a self-reference or a cycle. */
    void addGroupChild(String groupName, String childGroupName) throws SQLException;

    void removeGroupChild(String groupName, String childGroupName) throws SQLException;

    Set<String> getEffectiveGroupPermissions(String groupName) throws SQLException;

    Set<String> getPlayerGroups(String playerUuid) throws SQLException;

    Set<String> getAllRegisteredGroups() throws SQLException;

    AEGroup getDefaultGroup() throws SQLException;

    void addPlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException;

    void removePlayerPermissions(String playerUuid, Set<String> permissions) throws SQLException;

    void addGroupPermissions(String groupName, Set<String> permissions) throws SQLException;

    void removeGroupPermissions(String groupName, Set<String> permissions) throws SQLException;

    void addPlayerToGroup(String playerUuid, String groupName) throws SQLException;

    void removePlayerFromGroup(String playerUuid, String groupName) throws SQLException;

    void setPlayerGroup(String playerUuid, String groupName) throws SQLException;

    void setDefaultGroup(String groupName) throws SQLException;

    void invalidatePlayerPermissions(String playerUuid);

    void invalidatePlayerGroups(String playerUuid);

    Map<String, AEGroup> getCachedGroups();

    /** Every player UUID with a permissions footprint (a direct grant or a group membership) - not filtered by any specific node; wildcard resolution happens in {@link me.bronsk.aethercore.modules.permissions.AEPermissionsProvider}. */
    Set<String> getPermissionCandidateUuids() throws SQLException;

    /** Every distinct permission string currently granted to some group or some player - used to suggest known nodes when granting a new one. */
    Set<String> getAllKnownPermissions() throws SQLException;

    /** The world/expiry metadata attached to this player's membership in {@code groupName}, or null if none is set. */
    AEPlayerGroupMetadata getPlayerGroupMetadata(String playerUuid, String groupName) throws SQLException;

    /** Creates or updates the world/expiry metadata for this player's membership in {@code groupName} - false if the membership itself doesn't exist. */
    boolean savePlayerGroupMetadata(String playerUuid, String groupName, String worldName, Timestamp expiresAt) throws SQLException;

    /** The world/expiry metadata attached to this player's grant of {@code permission}, or null if none is set. */
    AEUserPermissionMetadata getPlayerPermissionMetadata(String playerUuid, String permission) throws SQLException;

    /** Creates or updates the world/expiry metadata for this player's grant of {@code permission} - false if the grant itself doesn't exist. */
    boolean savePlayerPermissionMetadata(String playerUuid, String permission, String worldName, Timestamp expiresAt) throws SQLException;

    /** The world/expiry metadata attached to this group's grant of {@code permission}, or null if none is set. */
    AEGroupPermissionMetadata getGroupPermissionMetadata(String groupName, String permission) throws SQLException;

    /** Creates or updates the world/expiry metadata for this group's grant of {@code permission} - false if the grant itself doesn't exist. */
    boolean saveGroupPermissionMetadata(String groupName, String permission, String worldName, Timestamp expiresAt) throws SQLException;

    /** Every direct-grant metadata row for this player that has metadata set, keyed by permission node - see {@code AEUsersPermissionsSqlStorage}. */
    Map<String, AEUserPermissionMetadata> getPlayerPermissionMetadataByNode(String playerUuid) throws SQLException;

    /** Every direct-grant metadata row for this group that has metadata set, keyed by permission node - see {@code AEGroupsPermissionsSqlStorage}. */
    Map<String, AEGroupPermissionMetadata> getGroupPermissionMetadataByNode(String groupName) throws SQLException;

    /** Physically deletes every already-expired direct player-permission grant - see {@code AEUsersPermissionsSqlStorage}. */
    int deleteExpiredPlayerPermissions() throws SQLException;

    /** Physically deletes every already-expired player-group membership - see {@code AEUsersPermissionsSqlStorage}. */
    int deleteExpiredPlayerGroups() throws SQLException;

    /** Physically deletes every already-expired group-permission grant - see {@code AEGroupsPermissionsSqlStorage}. */
    int deleteExpiredGroupPermissions() throws SQLException;

    /** Every group {@code playerUuid} directly belongs to, newest membership first, alongside when each membership was created - for the Player Groups tab's grid. */
    Map<String, Timestamp> getGroupCreatedAtByGroupName(String playerUuid) throws SQLException;

    /** Cascade-deletes a group, its own grants/edges/settings and every player's membership in it. Callers must guard against other groups still inheriting from it first - see {@code AEPermissionsGroupsModule.handleDeleteGroup}. */
    void deleteGroup(String groupName) throws SQLException;

    void clearCache();

    @Override
    void close();

}
