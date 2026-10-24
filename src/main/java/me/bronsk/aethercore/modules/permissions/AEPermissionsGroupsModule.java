package me.bronsk.aethercore.modules.permissions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.storage.AEPermissionsStorage;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEPlayer;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/** Owns every group-centric permissions operation: catalog, membership resolution, the inheritance DAG and the registered-group listing. */
public class AEPermissionsGroupsModule extends AESubModule<AEPermissionsModule, AEPermissionsStorage> {

    @Override
    public String getName() {
        return "groups";
    }

    /**
     * Adds permissions to a group and returns the command response message.
     *
     * @param groupName Group name.
     * @param permissions Permission nodes to add.
     * @return Result containing the response message and added permissions.
     */
    public AEActionResult handleAddGroupPermissions(AEExecutionContext context, String groupName, Set<String> permissions) {
        AEActionResult guard = checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        PermissionsModule.get().addGroupPermission(groupName, permissions);
        return AEActionResult.success(getLangModule().buildMessage("permissions.group.permission.add.success", context, args("group", groupName)));
    }

    /**
     * Removes permissions from a group and returns the command response message.
     *
     * @param groupName Group name.
     * @param permissions Permission nodes to remove.
     * @return Result containing the response message and removed permissions.
     */
    public AEActionResult handleRemoveGroupPermissions(AEExecutionContext context, String groupName, Set<String> permissions) {
        AEActionResult guard = checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        PermissionsModule.get().removeGroupPermission(groupName, permissions);
        return AEActionResult.success(getLangModule().buildMessage("permissions.group.permission.remove.success", context, args("group", groupName)));
    }

    /**
     * Returns the command response for listing group permissions.
     *
     * @param groupName Group name.
     * @return Result containing the response message and listed permissions.
     */
    public Message handleGetGroupPermissions(AEExecutionContext context, String groupName) {
        Function<com.hypixel.hytale.server.core.permissions.provider.PermissionProvider, Set<String>> valuesProvider =
                provider -> provider.getGroupPermissions(groupName);
        return getParentModule().handleGetProviderList(context, valuesProvider, "permissions.group.permissions.list.title");
    }

    /**
     * Persists permissions added to a group through the permission provider.
     *
     * @param groupName Group name.
     * @param permissions Permission nodes to add.
     */
    public void addGroupPermissions(String groupName, Set<String> permissions) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().addGroupPermissions(groupName, permissions));
    }

    /**
     * Persists permissions removed from a group through the permission provider.
     *
     * @param groupName Group name.
     * @param permissions Permission nodes to remove.
     */
    public void removeGroupPermissions(String groupName, Set<String> permissions) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().removeGroupPermissions(groupName, permissions));
    }

    /**
     * Returns permissions assigned directly to a group.
     *
     * @param groupName Group name.
     * @return Direct group permission nodes.
     */
    public Set<String> getGroupPermissions(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupPermissions(groupName));
    }

    /**
     * Same as {@link #getGroupPermissions} but with any currently-expired grant filtered out - the
     * "live" set {@link AEPermissionsProvider} hands to the SDK's own resolver. World restrictions on
     * a group's own grant can't be enforced here: this call carries no player context to compare a
     * world against (a group's permissions are resolved independently of who's asking) - only {@link
     * AEPermissionsUsersModule#getRuntimeUserPermissions} can, for a player's own direct grants.
     */
    public Set<String> getRuntimeGroupPermissions(String groupName) {
        Set<String> raw = getGroupPermissions(groupName);
        Map<String, AEGroupPermissionMetadata> metadataByNode =
                AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupPermissionMetadataByNode(groupName));
        if (metadataByNode.isEmpty()) {
            return raw;
        }
        Set<String> filtered = new LinkedHashSet<>();
        for (String node : raw) {
            AEGroupPermissionMetadata metadata = metadataByNode.get(node);
            if (metadata == null || AEPermissionsUsersModule.isCurrentlyValid(null, metadata.getExpiresAt(), null)) {
                filtered.add(node);
            }
        }
        return Collections.unmodifiableSet(filtered);
    }

    /** A group's own direct permission grants, newest first, as full rows - so {@code getCreatedAt()} is already there, no follow-up lookup needed. */
    public List<AEGroupPermission> getGroupPermissionsOrderedByRecent(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupPermissionsOrderedByRecent(groupName));
    }

    /** The world/expiry metadata attached to this group's grant of {@code permission}, or null if none is set. */
    public AEGroupPermissionMetadata getPermissionMetadata(String groupName, String permission) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupPermissionMetadata(groupName, permission));
    }

    /** Creates or updates the world/expiry metadata for this group's grant of {@code permission} - false if the grant itself doesn't exist. */
    public boolean savePermissionMetadata(String groupName, String permission, String worldName, Timestamp expiresAt) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().saveGroupPermissionMetadata(groupName, permission, worldName, expiresAt));
    }

    /**
     * Makes a group inherit from another and returns the command response message.
     *
     * @param groupName Group that will inherit.
     * @param childGroupName Group it will inherit from.
     * @return Result containing the response message.
     */
    public AEActionResult handleAddGroupChild(AEExecutionContext context, String groupName, String childGroupName) {
        AEActionResult guard = checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        try {
            AEDatabaseUtils.runUnchecked(() -> getStorage().addGroupChild(groupName, childGroupName));
            return AEActionResult.success(getLangModule().buildMessage("permissions.group.children.add.success", context, args("group", groupName, "child", childGroupName)));
        } catch (IllegalArgumentException exception) {
            return AEActionResult.error(getLangModule().buildMessage("permissions.group.children.add.failed", context, args("group", groupName, "error", exception.getMessage())));
        }
    }

    /**
     * Removes a group's inheritance from another and returns the command response message.
     *
     * @param groupName Group that currently inherits.
     * @param childGroupName Group it should stop inheriting from.
     * @return Result containing the response message.
     */
    public AEActionResult handleRemoveGroupChild(AEExecutionContext context, String groupName, String childGroupName) {
        AEActionResult guard = checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        AEDatabaseUtils.runUnchecked(() -> getStorage().removeGroupChild(groupName, childGroupName));
        return AEActionResult.success(getLangModule().buildMessage("permissions.group.children.remove.success", context, args("group", groupName, "child", childGroupName)));
    }

    /**
     * Returns the command response for listing a group's direct children.
     *
     * @param groupName Group name.
     * @return Result containing the response message.
     */
    public Message handleGetGroupChildren(AEExecutionContext context, String groupName) {
        Set<String> children = getGroupChildren(groupName);
        if (children.isEmpty()) {
            return getLangModule().buildMessage("permissions.group.children.list.empty", context, args("group", groupName));
        }
        return getLangModule().buildMessage("permissions.group.children.list.title", context, args("group", groupName, "children", String.join(", ", children)));
    }

    /** Groups {@code groupName} directly inherits from - one hop, not transitive. */
    public Set<String> getGroupChildren(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupChildren(groupName));
    }

    /** Every group reachable from {@code groupName} by following "inherits from" edges, transitively. */
    public Set<String> getTransitiveGroupChildren(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getTransitiveGroupChildren(groupName));
    }

    /** Groups {@code groupName} directly inherits from, newest edge first, alongside when each edge was created - for the Group Children tab's grid. */
    public Map<String, Timestamp> getGroupChildrenOrderedByRecent(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupChildrenOrderedByRecent(groupName));
    }

    /**
     * Returns every registered permission group.
     *
     * @return Registered group names.
     */
    public Set<String> getAllRegisteredGroups() {
        return AEDatabaseUtils.supplyUnchecked(getStorage()::getAllRegisteredGroups);
    }

    /** One page of registered groups for the Permissions tab's Groups table. */
    public AEPageResult<AEGroup> getGroupQueryResult(AEPageQuery query) {
        return AEDatabaseUtils.supplyUnchecked(() -> new AEPageResult<>(
                query,
                getStorage().queryGroups(query),
                getStorage().countGroups(query)));
    }

    public AEGroup getGroup(String name) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroup(name));
    }

    /** Registers a brand-new group - fails without creating anything if the name is already taken. */
    public AEActionResult handleCreateGroup(AEExecutionContext context, String name) {
        if (getGroup(name) != null) {
            return AEActionResult.error(getLangModule().buildMessage("permissions.group.exists", context, args("group", name)));
        }
        AEDatabaseUtils.runUnchecked(() -> getStorage().getOrCreateGroup(name));
        return AEActionResult.success(getLangModule().buildMessage("permissions.group.create.success", context, args("group", name)));
    }

    /**
     * Deletes a group and everything that directly depends on it (its own grants, its own "inherits
     * from" edges, its settings, every player's membership in it). Refuses when {@code groupName}
     * outranks the actor, is the current default group (would leave no default), or is still relied
     * on by another group's inheritance edge (deleting it would silently break that other group's
     * effective permissions, and would also leave a dangling foreign key at the DB level).
     */
    public AEActionResult handleDeleteGroup(AEExecutionContext context, String groupName) {
        AEActionResult guard = checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        if (getGroup(groupName) == null) {
            return AEActionResult.error(getLangModule().buildMessage("permissions.group.missing", context, args("group", groupName)));
        }
        AEGroup defaultGroup = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getDefaultGroup());
        if (defaultGroup != null && defaultGroup.getName().equals(groupName)) {
            return AEActionResult.error(getLangModule().buildMessage("permissions.group.delete.default", context, args("group", groupName)));
        }
        Set<String> dependents = getDirectDependentGroups(groupName);
        if (!dependents.isEmpty()) {
            return AEActionResult.error(getLangModule().buildMessage("permissions.group.delete.has_dependents", context,
                    args("group", groupName, "dependents", String.join(", ", dependents))));
        }
        AEDatabaseUtils.runUnchecked(() -> getStorage().deleteGroup(groupName));
        return AEActionResult.success(getLangModule().buildMessage("permissions.group.delete.success", context, args("group", groupName)));
    }

    /** Every registered group whose direct "inherits from" edge points at {@code groupName} - i.e. every group that would be left with a dangling reference if it were deleted. */
    private Set<String> getDirectDependentGroups(String groupName) {
        Set<String> dependents = new LinkedHashSet<>();
        for (String candidate : getAllRegisteredGroups()) {
            if (getGroupChildren(candidate).contains(groupName)) {
                dependents.add(candidate);
            }
        }
        return dependents;
    }

    /** How many players are currently in {@code groupName} - cheaper than {@link #getPlayersInGroup} when only the count is needed (no per-player resolution). */
    public int getGroupMemberCount(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerGroupCreatedAtByPlayerUuid(groupName)).size();
    }

    /** Every player currently in {@code groupName}, resolved to full player records, alongside when each membership was created - keyed by UUID (not the {@link AEPlayer} instance, which has no {@code equals}/{@code hashCode} of its own), unpaginated, the caller pages/searches in Java. */
    public LinkedHashMap<String, Map.Entry<AEPlayer, Timestamp>> getPlayersInGroup(String groupName) {
        Map<String, Timestamp> createdAtByUuid = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerGroupCreatedAtByPlayerUuid(groupName));
        AEPlayersModule playersModule = getModulesManager().getModule(AEPlayersModule.class);
        LinkedHashMap<String, Map.Entry<AEPlayer, Timestamp>> players = new LinkedHashMap<>();
        for (Map.Entry<String, Timestamp> entry : createdAtByUuid.entrySet()) {
            AEPlayer player = playersModule.getPlayerByUuid(UUID.fromString(entry.getKey()));
            if (player != null) {
                players.put(entry.getKey(), new AbstractMap.SimpleEntry<>(player, entry.getValue()));
            }
        }
        return players;
    }

    /**
     * Returns direct and inherited permissions for a group.
     *
     * @param groupName Group name.
     * @return Effective permission nodes.
     */
    public Set<String> getEffectiveGroupPermissions(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getEffectiveGroupPermissions(groupName));
    }

    /**
     * Sets the default group and returns the command response message.
     *
     * @param groupName Group name.
     * @return Result containing the response message and default group name.
     */
    public Message handleSetDefaultGroup(AEExecutionContext context, String groupName) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().setDefaultGroup(groupName));
        return getLangModule().buildMessage("permissions.group.default.success", context, args("group", groupName));
    }

    /**
     * Installs the default group when none exists.
     *
     * @param groupName Default group name.
     * @throws SQLException when the group cannot be installed.
     */
    public void installDefaultGroup(String groupName) throws SQLException {
        if (getStorage().getDefaultGroup() == null) {
            getStorage().setDefaultGroup(groupName);
        }
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }

    /**
     * Returns an error result when {@code groupName} is at or above the actor's own rank and
     * therefore off-limits, or {@code null} when the action may proceed. A group's "rank" is
     * structural, not the {@code priority} setting: for each group the actor directly belongs to,
     * discard any that is itself reachable (transitively, via {@link #getTransitiveGroupChildren})
     * from another one they belong to - leaving only the most-privileged owned group per
     * inheritance branch. The actor may not touch that group, nor any group that (transitively)
     * inherits from it - "X inherits from Y" always means X's effective permissions are a superset
     * of Y's, regardless of what else X also inherits from, so this holds even with multiple
     * inheritance. Console and any actor holding the {@code "*"} wildcard bypass this entirely.
     */
    AEActionResult checkGroupModifiable(AEExecutionContext context, String groupName) {
        if (!isGroupProtectedForActor(context, groupName)) {
            return null;
        }
        return AEActionResult.error(getLangModule().buildMessage("permissions.group.protected", context, args("group", groupName)));
    }

    private boolean isGroupProtectedForActor(AEExecutionContext context, String groupName) {
        if (context.actorUuid() == null) {
            return false;
        }
        AEPermissionsUsersModule users = getParentModule().getUsersModule();
        if (users.getUserPermissions(context.actorUuid()).contains("*")) {
            return false;
        }
        Set<String> ownGroups = users.getGroupsForUser(context.actorUuid());
        if (ownGroups.isEmpty()) {
            return false;
        }
        for (String own : maximalOwnGroups(ownGroups)) {
            if (own.equals(groupName) || getGroupDescendants(own).contains(groupName)) {
                return true;
            }
        }
        return false;
    }

    /** Discards any owned group that is itself reachable (transitively) from another owned group, leaving only the most-privileged owned group per inheritance branch. */
    private Set<String> maximalOwnGroups(Set<String> ownGroups) {
        Set<String> maximal = new LinkedHashSet<>();
        for (String candidate : ownGroups) {
            boolean isReachableFromAnother = false;
            for (String other : ownGroups) {
                if (!other.equals(candidate) && getTransitiveGroupChildren(other).contains(candidate)) {
                    isReachableFromAnother = true;
                    break;
                }
            }
            if (!isReachableFromAnother) {
                maximal.add(candidate);
            }
        }
        return maximal;
    }

    /** Every registered group that (transitively) inherits from {@code groupName} - i.e. every group more privileged than it. */
    private Set<String> getGroupDescendants(String groupName) {
        Set<String> descendants = new HashSet<>();
        for (String candidate : getAllRegisteredGroups()) {
            if (getTransitiveGroupChildren(candidate).contains(groupName)) {
                descendants.add(candidate);
            }
        }
        return descendants;
    }
}
