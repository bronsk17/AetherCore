package me.bronsk.aethercore.modules.permissions.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.commands.arguments.AEGroupTypeArg;
import me.bronsk.aethercore.commands.AECollectionCommand;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsPerms;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.arguments.AEPlayerArg;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AEPermUserCommand extends AECollectionCommand {

    /**
     * Creates user permission commands.
     */
    public AEPermUserCommand() {
        super("user", "server.commands.perm.user.desc");
        addSubCommand(new AEPermUserListCommand());
        addSubCommand(new AEPermUserAddCommand());
        addSubCommand(new AEPermUserRemoveCommand());
        addSubCommand(new AEPermUserGrantCommand());
        addSubCommand(new AEPermUserGroupCommand());
    }

    private abstract static class AEPermUserBaseCommand extends AECommand {

        protected final RequiredArg<String> playerArg;

        /**
         * Creates a user permission subcommand.
         *
         * @param name Command name.
         * @param description Command description.
         */
        protected AEPermUserBaseCommand(String name, String description, String permission) {
            super(name, description);
            requirePermission(permission);
            this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
        }

        /**
         * Runs user permission commands on the target player's world executor when that player is online. The SDK
         * permission module dispatches events that may read components from the mutated player.
         *
         * @param commandContext Command execution context.
         * @return Completion future for the command execution.
         */
        @Override
        protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
            AEPlayersModule tcPlayersModule = AEModulesManager.get().getModule(AEPlayersModule.class);
            UUID uuid = tcPlayersModule.resolvePlayerUuid(playerArg.get(commandContext));
            if (uuid == null) {
                executeSync(commandContext);
                return CompletableFuture.completedFuture(null);
            }

            return executeOnOnlinePlayerWorld(uuid, () -> executeSync(commandContext));
        }
    }

    private static class AEPermUserListCommand extends AEPermUserBaseCommand {

        /**
         * Creates the user permission list command.
         */
        private AEPermUserListCommand() {
            super("list", "server.commands.perm.user.list.desc", AEPermissionsPerms.USER_LIST);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleListEffectivePermissions(getExecutionContext(commandContext), player));
        }
    }

    private static class AEPermUserAddCommand extends AEPermUserBaseCommand {

        private final RequiredArg<java.util.List<String>> permissionsArg;

        /**
         * Creates the user permission add command.
         */
        private AEPermUserAddCommand() {
            super("add", "server.commands.perm.user.add.desc", AEPermissionsPerms.USER_ADD);
            this.permissionsArg = withListRequiredArg("permissions", "server.commands.perm.user.add.permissions.desc", ArgTypes.STRING);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            Set<String> permissions = new HashSet<>(permissionsArg.get(commandContext));
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleAddUserPermissions(getExecutionContext(commandContext), player, permissions).message());
        }
    }

    private static class AEPermUserRemoveCommand extends AEPermUserBaseCommand {

        private final RequiredArg<java.util.List<String>> permissionsArg;

        /**
         * Creates the user permission remove command.
         */
        private AEPermUserRemoveCommand() {
            super("remove", "server.commands.perm.user.remove.desc", AEPermissionsPerms.USER_REMOVE);
            this.permissionsArg = withListRequiredArg("permissions", "server.commands.perm.user.remove.permissions.desc", ArgTypes.STRING);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            Set<String> permissions = new HashSet<>((Collection<String>) permissionsArg.get(commandContext));
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleRemoveUserPermissions(getExecutionContext(commandContext), player, permissions).message());
        }
    }

    /** Grants one permission with an optional world restriction and/or expiry - {@code add} stays the plain, metadata-less bulk grant. */
    private static class AEPermUserGrantCommand extends AEPermUserBaseCommand {

        private final RequiredArg<String> permissionArg;
        private final OptionalArg<String> worldArg;
        private final OptionalArg<Duration> durationArg;

        private AEPermUserGrantCommand() {
            super("grant", "Grants one permission, optionally restricted to a world and/or with an expiry", AEPermissionsPerms.USER_ADD);
            this.permissionArg = withRequiredArg("permission", "Permission node", ArgTypes.STRING);
            this.worldArg = withOptionalArg("world", "World this grant is restricted to", ArgTypes.STRING);
            this.durationArg = withOptionalArg("duration", "Expires after this long, e.g. 10m, 2h, 7d", AEDurationArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            String permission = permissionArg.get(commandContext);
            String world = worldArg.get(commandContext);
            Duration duration = durationArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            AEActionResult result = tcPermissionsModule.getUsersModule().handleAddUserPermissions(
                    getExecutionContext(commandContext), player, Set.of(permission));
            if (result.success() && (world != null || duration != null)) {
                UUID uuid = AEModulesManager.get().getModule(AEPlayersModule.class).resolvePlayerUuid(player);
                if (uuid != null) {
                    Timestamp expiresAt = duration == null ? null : new Timestamp(System.currentTimeMillis() + duration.toMillis());
                    tcPermissionsModule.getUsersModule().savePermissionMetadata(uuid, permission, world, expiresAt);
                }
            }
            reply(commandContext, result.message());
        }
    }

    private static class AEPermUserGroupCommand extends AECollectionCommand {

        /**
         * Creates user group commands.
         */
        private AEPermUserGroupCommand() {
            super("group", "server.commands.perm.user.group.desc");
            addSubCommand(new AEPermUserGroupListCommand());
            addSubCommand(new AEPermUserGroupAddCommand());
            addSubCommand(new AEPermUserGroupRemoveCommand());
            addSubCommand(new AEPermUserGroupGrantCommand());
        }
    }

    private static class AEPermUserGroupListCommand extends AEPermUserBaseCommand {

        /**
         * Creates the user group list command.
         */
        private AEPermUserGroupListCommand() {
            super("list", "server.commands.perm.user.group.list.desc", AEPermissionsPerms.USER_GROUP_LIST);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleGetUserGroups(getExecutionContext(commandContext), player));
        }
    }

    private static class AEPermUserGroupAddCommand extends AEPermUserBaseCommand {

        private final RequiredArg<String> groupArg;

        /**
         * Creates the user group add command.
         */
        private AEPermUserGroupAddCommand() {
            super("add", "server.commands.perm.user.group.add.desc", AEPermissionsPerms.USER_GROUP_ADD);
            this.groupArg = withRequiredArg("group", "server.commands.perm.user.group.add.group.desc", AEGroupTypeArg.INSTANCE);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleAddUserToGroup(getExecutionContext(commandContext), player, group).message());
        }
    }

    private static class AEPermUserGroupRemoveCommand extends AEPermUserBaseCommand {

        private final RequiredArg<String> groupArg;

        /**
         * Creates the user group remove command.
         */
        private AEPermUserGroupRemoveCommand() {
            super("remove", "server.commands.perm.user.group.remove.desc", AEPermissionsPerms.USER_GROUP_REMOVE);
            this.groupArg = withRequiredArg("group", "server.commands.perm.user.group.remove.group.desc", AEGroupTypeArg.INSTANCE);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getUsersModule().handleRemoveUserFromGroup(getExecutionContext(commandContext), player, group).message());
        }
    }

    /** Adds a player to a group with an optional world restriction and/or expiry - {@code add} stays the plain, metadata-less membership grant. */
    private static class AEPermUserGroupGrantCommand extends AEPermUserBaseCommand {

        private final RequiredArg<String> groupArg;
        private final OptionalArg<String> worldArg;
        private final OptionalArg<Duration> durationArg;

        private AEPermUserGroupGrantCommand() {
            super("grant", "Adds a player to a group, optionally restricted to a world and/or with an expiry", AEPermissionsPerms.USER_GROUP_ADD);
            this.groupArg = withRequiredArg("group", "Group name", AEGroupTypeArg.INSTANCE);
            this.worldArg = withOptionalArg("world", "World this membership is restricted to", ArgTypes.STRING);
            this.durationArg = withOptionalArg("duration", "Expires after this long, e.g. 10m, 2h, 7d", AEDurationArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String player = playerArg.get(commandContext);
            String group = groupArg.get(commandContext);
            String world = worldArg.get(commandContext);
            Duration duration = durationArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            AEActionResult result = tcPermissionsModule.getUsersModule().handleAddUserToGroup(getExecutionContext(commandContext), player, group);
            if (result.success() && (world != null || duration != null)) {
                UUID uuid = AEModulesManager.get().getModule(AEPlayersModule.class).resolvePlayerUuid(player);
                if (uuid != null) {
                    Timestamp expiresAt = duration == null ? null : new Timestamp(System.currentTimeMillis() + duration.toMillis());
                    tcPermissionsModule.getUsersModule().saveGroupMetadata(uuid, group, world, expiresAt);
                }
            }
            reply(commandContext, result.message());
        }
    }
}
