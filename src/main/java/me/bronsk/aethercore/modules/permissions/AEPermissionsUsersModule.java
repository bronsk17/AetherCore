package me.bronsk.aethercore.modules.permissions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.storage.AEPermissionsStorage;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayersModule;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/** Owns every player-centric permissions operation: direct grants, group membership and the effective-permissions listing. */
public class AEPermissionsUsersModule extends AESubModule<AEPermissionsModule, AEPermissionsStorage> {

    @Override
    public String getName() {
        return "users";
    }

    /**
     * Adds permissions to a user and returns the command response message.
     *
     * @param player Player UUID, online name, or stored name.
     * @param permissions Permission nodes to add.
     * @return Result containing the response message and resolved player UUID.
     */
    public AEActionResult handleAddUserPermissions(AEExecutionContext context, String player, Set<String> permissions) {
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return AEActionResult.error(getMissingUserMessage(context, player));
        }
        PermissionsModule.get().addUserPermission(uuid, permissions);
        return AEActionResult.success(getLangModule().buildMessage("permissions.user.permission.add.success", context, args("target", uuid.toString())));
    }

    /**
     * Removes permissions from a user and returns the command response message.
     *
     * @param player Player UUID, online name, or stored name.
     * @param permissions Permission nodes to remove.
     * @return Result containing the response message and resolved player UUID.
     */
    public AEActionResult handleRemoveUserPermissions(AEExecutionContext context, String player, Set<String> permissions) {
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return AEActionResult.error(getMissingUserMessage(context, player));
        }
        PermissionsModule.get().removeUserPermission(uuid, permissions);
        return AEActionResult.success(getLangModule().buildMessage("permissions.user.permission.remove.success", context, args("target", uuid.toString())));
    }

    /**
     * Returns the command response listing the caller's own direct, group, and inherited permissions.
     *
     * @return Result containing the response message.
     */
    public Message handleListEffectivePermissions(AEExecutionContext context) {
        if (context.actorUuid() == null) {
            return getLangModule().buildMessage("permissions.list.player.only", context);
        }
        return buildEffectivePermissionsMessage(context, context.actorUuid());
    }

    /**
     * Returns the command response listing a player's direct, group, and inherited permissions.
     *
     * @param player Player UUID, online name, or stored name.
     * @return Result containing the response message.
     */
    public Message handleListEffectivePermissions(AEExecutionContext context, String player) {
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingUserMessage(context, player);
        }
        return buildEffectivePermissionsMessage(context, uuid);
    }

    /**
     * Builds the direct group, direct user, and inherited permissions breakdown for a player.
     *
     * @param uuid Player UUID.
     * @return Result containing the response message.
     */
    private Message buildEffectivePermissionsMessage(AEExecutionContext context, UUID uuid) {
        AEPermissionsGroupsModule groups = getParentModule().getGroupsModule();
        Set<String> groupNames = getGroupsForUser(uuid);
        Message response = Message.empty();

        response.insert(getLangModule().buildMessage("permissions.list.groups.header", context));
        for (String groupName : groupNames) {
            response.insert("\n");
            response.insert(buildGroupPermissionsEntry(context, groupName));
        }

        response.insert("\n");
        response.insert(getLangModule().buildMessage("permissions.list.user.header", context));
        response.insert("\n");
        response.insert(MessageFormat.list(Message.empty(), toMessages(getUserPermissions(uuid))));

        response.insert("\n");
        response.insert(getLangModule().buildMessage("permissions.list.inherited.header", context));
        Set<String> shownInherited = new LinkedHashSet<>();
        for (String groupName : groupNames) {
            for (String inherited : groups.getTransitiveGroupChildren(groupName)) {
                if (!shownInherited.add(inherited) || groups.getGroupPermissions(inherited).isEmpty()) {
                    continue;
                }
                response.insert("\n");
                response.insert(buildGroupPermissionsEntry(context, inherited));
            }
        }

        return response;
    }

    /**
     * Builds a single "group (priority): permissions" entry.
     *
     * @param groupName Group name.
     * @return Message listing the group's direct permissions under its name and priority.
     */
    private Message buildGroupPermissionsEntry(AEExecutionContext context, String groupName) {
        Integer priority = getParentModule().getGroupSettingsModule().getGroupPriority(groupName);
        Message title = getLangModule().buildMessage("permissions.list.group.entry", context, args("group", groupName, "priority", String.valueOf(priority == null ? 0 : priority)));
        return MessageFormat.list(title, toMessages(getParentModule().getGroupsModule().getGroupPermissions(groupName)));
    }

    private static Set<Message> toMessages(Set<String> permissions) {
        return permissions.stream().map(Message::raw).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Adds permissions directly to a player through the permission provider.
     *
     * @param uuid Player UUID.
     * @param permissions Permission nodes to add.
     */
    public void addUserPermissions(UUID uuid, Set<String> permissions) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().addPlayerPermissions(uuid.toString(), permissions));
    }

    /**
     * Removes direct permissions from a player through the permission provider.
     *
     * @param uuid Player UUID.
     * @param permissions Permission nodes to remove.
     */
    public void removeUserPermissions(UUID uuid, Set<String> permissions) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().removePlayerPermissions(uuid.toString(), permissions));
    }

    /**
     * Returns permissions assigned directly to a player.
     *
     * @param uuid Player UUID.
     * @return Direct player permission nodes, or an empty set when the player is unknown.
     */
    public Set<String> getUserPermissions(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerPermissions(uuid.toString()));
    }

    /**
     * Same as {@link #getUserPermissions} but with any of the player's own DIRECT grants that are
     * currently world/expiry-restricted filtered out - the "live" set {@link AEPermissionsProvider}
     * hands to the SDK's own resolver. Only the direct-grant half of a world/expiry restriction is
     * enforceable here: a permission reaching this player purely through group inheritance carries
     * no per-node metadata of its own once merged, so a restriction on the *group's* grant only takes
     * effect when that group's permissions are asked for directly - see {@link
     * AEPermissionsGroupsModule#getRuntimeGroupPermissions}.
     */
    public Set<String> getRuntimeUserPermissions(UUID uuid) {
        Set<String> raw = getUserPermissions(uuid);
        Map<String, AEUserPermissionMetadata> metadataByNode =
                AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerPermissionMetadataByNode(uuid.toString()));
        if (metadataByNode.isEmpty()) {
            return raw;
        }
        String currentWorld = resolveCurrentWorld(uuid);
        Set<String> filtered = new LinkedHashSet<>();
        for (String node : raw) {
            AEUserPermissionMetadata metadata = metadataByNode.get(node);
            if (metadata == null || isCurrentlyValid(metadata.getWorldName(), metadata.getExpiresAt(), currentWorld)) {
                filtered.add(node);
            }
        }
        return Collections.unmodifiableSet(filtered);
    }

    /** The name of the world {@code uuid} is currently standing in, or null when they're offline or it can't be resolved - a world restriction never excludes a grant when the current world is unknown. */
    private static String resolveCurrentWorld(UUID uuid) {
        AEGamePlayer player = AEGamePlayer.fromUuid(uuid);
        if (player == null) {
            return null;
        }
        try {
            return player.getWorld().getName();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    /** True unless the metadata excludes the grant right now: expired (has an expiry that isn't in the future), or world-restricted to a different world than {@code currentWorld}. */
    static boolean isCurrentlyValid(String metadataWorld, Timestamp expiresAt, String currentWorld) {
        if (expiresAt != null && !expiresAt.after(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }
        return metadataWorld == null || metadataWorld.isBlank() || currentWorld == null || metadataWorld.equalsIgnoreCase(currentWorld);
    }

    /** A player's own direct permissions, not merged with anything granted via their groups. */
    public Set<String> getDirectUserPermissions(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getDirectPlayerPermissions(uuid.toString()));
    }

    /** A player's own direct permission grants, newest first, as full rows - so {@code getCreatedAt()} is already there, no follow-up lookup needed. */
    public List<AEPlayerPermission> getDirectUserPermissionsOrderedByRecent(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getDirectPlayerPermissionsOrderedByRecent(uuid.toString()));
    }

    /**
     * Adds a user to a group and returns the command response message.
     *
     * @param player Player UUID, online name, or stored name.
     * @param groupName Group name.
     * @return Result containing the response message and resolved player UUID.
     */
    public AEActionResult handleAddUserToGroup(AEExecutionContext context, String player, String groupName) {
        AEActionResult guard = getParentModule().getGroupsModule().checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return AEActionResult.error(getMissingUserMessage(context, player));
        }
        PermissionsModule.get().addUserToGroup(uuid, groupName);
        return AEActionResult.success(getLangModule().buildMessage("permissions.user.group.add.success", context, args("target", uuid.toString(), "group", groupName)));
    }

    /**
     * Removes a user from a group and returns the command response message.
     *
     * @param player Player UUID, online name, or stored name.
     * @param groupName Group name.
     * @return Result containing the response message and resolved player UUID.
     */
    public AEActionResult handleRemoveUserFromGroup(AEExecutionContext context, String player, String groupName) {
        AEActionResult guard = getParentModule().getGroupsModule().checkGroupModifiable(context, groupName);
        if (guard != null) {
            return guard;
        }
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return AEActionResult.error(getMissingUserMessage(context, player));
        }
        PermissionsModule.get().removeUserFromGroup(uuid, groupName);
        return AEActionResult.success(getLangModule().buildMessage("permissions.user.group.remove.success", context, args("target", uuid.toString(), "group", groupName)));
    }

    /**
     * Returns the command response for listing a user's groups.
     *
     * @param player Player UUID, online name, or stored name.
     * @return Result containing the response message and listed groups.
     */
    public Message handleGetUserGroups(AEExecutionContext context, String player) {
        UUID uuid = resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingUserMessage(context, player);
        }

        Function<PermissionProvider, Set<String>> valuesProvider = provider -> provider.getGroupsForUser(uuid);
        return getParentModule().handleGetProviderList(context, valuesProvider, "permissions.user.groups.list.title");
    }

    /**
     * Adds a player to a group through the permission provider.
     *
     * @param uuid Player UUID.
     * @param groupName Group name.
     */
    public void addUserToGroup(UUID uuid, String groupName) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().addPlayerToGroup(uuid.toString(), groupName));
    }

    /**
     * Adds a player to the configured default group when the relation is missing.
     *
     * @param uuid Player UUID.
     */
    public void assignDefaultGroupIfNeeded(UUID uuid) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEGroup defaultGroup = getStorage().getDefaultGroup();
            if (defaultGroup != null) {
                getStorage().addPlayerToGroup(uuid.toString(), defaultGroup.getName());
            }
        });
    }

    /**
     * Removes a player from a group through the permission provider.
     *
     * @param uuid Player UUID.
     * @param groupName Group name.
     */
    public void removeUserFromGroup(UUID uuid, String groupName) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().removePlayerFromGroup(uuid.toString(), groupName));
    }

    /**
     * Replaces the player's group membership with a single group.
     *
     * @param uuid Player UUID.
     * @param groupName Group name to assign.
     */
    public void setUserGroup(UUID uuid, String groupName) {
        AEDatabaseUtils.runUnchecked(() -> getStorage().setPlayerGroup(uuid.toString(), groupName));
    }

    /**
     * Returns all groups assigned to a player.
     *
     * @param uuid Player UUID.
     * @return Group names, or an empty set when the player is unknown.
     */
    public Set<String> getGroupsForUser(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerGroups(uuid.toString()));
    }

    /** The world/expiry metadata attached to this player's membership in {@code groupName}, or null if none is set. */
    public AEPlayerGroupMetadata getGroupMetadata(UUID uuid, String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerGroupMetadata(uuid.toString(), groupName));
    }

    /** Creates or updates the world/expiry metadata for this player's membership in {@code groupName} - false if the membership itself doesn't exist. */
    public boolean saveGroupMetadata(UUID uuid, String groupName, String worldName, Timestamp expiresAt) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().savePlayerGroupMetadata(uuid.toString(), groupName, worldName, expiresAt));
    }

    /** The world/expiry metadata attached to this player's grant of {@code permission}, or null if none is set. */
    public AEUserPermissionMetadata getPermissionMetadata(UUID uuid, String permission) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayerPermissionMetadata(uuid.toString(), permission));
    }

    /** Creates or updates the world/expiry metadata for this player's grant of {@code permission} - false if the grant itself doesn't exist. */
    public boolean savePermissionMetadata(UUID uuid, String permission, String worldName, Timestamp expiresAt) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().savePlayerPermissionMetadata(uuid.toString(), permission, worldName, expiresAt));
    }

    /** Every group this player directly belongs to, newest membership first, alongside when each membership was created - for the Player Groups tab's grid. */
    public Map<String, Timestamp> getGroupsForUserOrderedByRecent(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupCreatedAtByGroupName(uuid.toString()));
    }

    private UUID resolvePlayerUuid(String player) {
        AEPlayersModule playersModule = getModulesManager().getModule(AEPlayersModule.class);
        return playersModule.resolvePlayerUuid(player);
    }

    private Message getMissingUserMessage(AEExecutionContext context, String player) {
        return getLangModule().buildMessage("permissions.user.missing", context, args("target", player));
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
