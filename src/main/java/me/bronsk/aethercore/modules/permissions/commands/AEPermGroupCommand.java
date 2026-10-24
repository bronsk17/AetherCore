package me.bronsk.aethercore.modules.permissions.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.commands.arguments.AEGroupTypeArg;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.commands.AECollectionCommand;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsPerms;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AEPermGroupCommand extends AECollectionCommand {

    /**
     * Creates a new instance.
     */
    public AEPermGroupCommand() {
        super("group", "server.commands.perm.group.desc");
        addSubCommand(new AEPermGroupListCommand());
        addSubCommand(new AEPermGroupCreateCommand());
        addSubCommand(new AEPermGroupDeleteCommand());
        addSubCommand(new AEPermGroupAddCommand());
        addSubCommand(new AEPermGroupRemoveCommand());
        addSubCommand(new AEPermGroupGrantCommand());
        addSubCommand(new AEPermGroupChildrenCommand());
        addSubCommand(new AEPermGroupDefaultCommand());
        addSubCommand(new AEPermGroupPrefixCommand());
        addSubCommand(new AEPermGroupSuffixCommand());
        addSubCommand(new AEPermGroupPriorityCommand());
    }

    private static class AEPermGroupCreateCommand extends AECommand {

        private final RequiredArg<String> groupArg;

        private AEPermGroupCreateCommand() {
            super("create", "Creates a new, empty permission group");
            requirePermission(AEPermissionsPerms.GROUP_CREATE);
            this.groupArg = withRequiredArg("group", "Name for the new group", ArgTypes.STRING);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleCreateGroup(getExecutionContext(commandContext), group).message());
        }
    }

    private static class AEPermGroupDeleteCommand extends AECommand {

        private final RequiredArg<String> groupArg;

        private AEPermGroupDeleteCommand() {
            super("delete", "Deletes a group, its own grants, and every player's membership in it");
            requirePermission(AEPermissionsPerms.GROUP_DELETE);
            this.groupArg = withRequiredArg("group", "Group to delete", AEGroupTypeArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleDeleteGroup(getExecutionContext(commandContext), group).message());
        }
    }

    /** Grants one permission with an optional world restriction and/or expiry - {@code add} stays the plain, metadata-less bulk grant. */
    private static class AEPermGroupGrantCommand extends AECommand {

        private final RequiredArg<String> groupArg;
        private final RequiredArg<String> permissionArg;
        private OptionalArg<String> worldArg;
        private OptionalArg<Duration> durationArg;

        private AEPermGroupGrantCommand() {
            super("grant", "Grants one permission, optionally restricted to a world and/or with an expiry");
            requirePermission(AEPermissionsPerms.GROUP_ADD);
            this.groupArg = withRequiredArg("group", "Group name", AEGroupTypeArg.INSTANCE);
            this.permissionArg = withRequiredArg("permission", "Permission node", ArgTypes.STRING);
            this.worldArg = withOptionalArg("world", "World this grant is restricted to", ArgTypes.STRING);
            this.durationArg = withOptionalArg("duration", "Expires after this long, e.g. 10m, 2h, 7d", AEDurationArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            String permission = permissionArg.get(commandContext);
            String world = worldArg.get(commandContext);
            Duration duration = durationArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            AEActionResult result = tcPermissionsModule.getGroupsModule().handleAddGroupPermissions(
                    getExecutionContext(commandContext), group, Set.of(permission));
            if (result.success() && (world != null || duration != null)) {
                Timestamp expiresAt = duration == null ? null : new Timestamp(System.currentTimeMillis() + duration.toMillis());
                tcPermissionsModule.getGroupsModule().savePermissionMetadata(group, permission, world, expiresAt);
            }
            reply(commandContext, result.message());
        }
    }

    private static class AEPermGroupListCommand extends AECommand {

        private final RequiredArg<String> groupArg;

        /**
         * Creates the permission group list command.
         */
        private AEPermGroupListCommand() {
            super("list", "server.commands.perm.group.list.desc");
            requirePermission(AEPermissionsPerms.GROUP_LIST);
            this.groupArg = withRequiredArg("group", "server.commands.perm.group.list.group.desc", AEGroupTypeArg.INSTANCE);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleGetGroupPermissions(getExecutionContext(commandContext), group));
        }
    }

    private static class AEPermGroupAddCommand extends AECommand {

        private final RequiredArg<String> groupArg;
        private final RequiredArg<java.util.List<String>> permissionsArg;

        /**
         * Creates the permission group add command.
         */
        private AEPermGroupAddCommand() {
            super("add", "server.commands.perm.group.add.desc");
            requirePermission(AEPermissionsPerms.GROUP_ADD);
            this.groupArg = withRequiredArg("group", "server.commands.perm.group.add.group.desc", ArgTypes.STRING);
            this.permissionsArg = withListRequiredArg("permissions", "server.commands.perm.group.add.permissions.desc", ArgTypes.STRING);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            Set<String> permissions = new HashSet<>(permissionsArg.get(commandContext));
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleAddGroupPermissions(getExecutionContext(commandContext), group, permissions).message());
        }
    }

    private static class AEPermGroupRemoveCommand extends AECommand {

        private final RequiredArg<String> groupArg;
        private final RequiredArg<java.util.List<String>> permissionsArg;

        /**
         * Creates the permission group remove command.
         */
        private AEPermGroupRemoveCommand() {
            super("remove", "server.commands.perm.group.remove.desc");
            requirePermission(AEPermissionsPerms.GROUP_REMOVE);
            this.groupArg = withRequiredArg("group", "server.commands.perm.group.remove.group.desc", AEGroupTypeArg.INSTANCE);
            this.permissionsArg = withListRequiredArg("permissions", "server.commands.perm.group.remove.permissions.desc", ArgTypes.STRING);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            Set<String> permissions = new HashSet<>((Collection<String>) permissionsArg.get(commandContext));
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleRemoveGroupPermissions(getExecutionContext(commandContext), group, permissions).message());
        }
    }

    private static class AEPermGroupDefaultCommand extends AECommand {

        private final RequiredArg<String> groupArg;

        /**
         * Creates the group default command.
         *
         */
        private AEPermGroupDefaultCommand() {
            super("default", "Set default group");
            requirePermission(AEPermissionsPerms.GROUP_DEFAULT);
            this.groupArg = withRequiredArg("group", "Default group", AEGroupTypeArg.INSTANCE);
        }

        /**
         * Handles executeSync.
         *
         * @param commandContext Method argument.
         */
        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleSetDefaultGroup(getExecutionContext(commandContext), group));
        }
    }

    private static class AEPermGroupChildrenCommand extends AECollectionCommand {

        /**
         * Creates the permission group children command collection.
         */
        private AEPermGroupChildrenCommand() {
            super("children", "Manage which groups this group inherits from");
            addSubCommand(new AEPermGroupChildrenAddCommand());
            addSubCommand(new AEPermGroupChildrenRemoveCommand());
            addSubCommand(new AEPermGroupChildrenListCommand());
        }
    }

    private abstract static class AEPermGroupSettingCommand extends AECommand {

        final RequiredArg<String> groupArg;

        private AEPermGroupSettingCommand(String name, String description, String permission) {
            super(name, description);
            requirePermission(permission);
            this.groupArg = withRequiredArg("group", "Group name", AEGroupTypeArg.INSTANCE);
        }
    }

    private static class AEPermGroupPrefixCommand extends AECollectionCommand {

        private AEPermGroupPrefixCommand() {
            super("prefix", "Manage group prefix");
            addSubCommand(new AEPermGroupPrefixGetCommand());
            addSubCommand(new AEPermGroupPrefixSetCommand());
            addSubCommand(new AEPermGroupPrefixResetCommand());
        }
    }

    private static class AEPermGroupPrefixGetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupPrefixGetCommand() {
            super("get", "Get group prefix", AEPermissionsPerms.GROUP_PREFIX);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleGetGroupPrefix(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupPrefixSetCommand extends AEPermGroupSettingCommand {

        private final RequiredArg<String> valueArg;

        private AEPermGroupPrefixSetCommand() {
            super("set", "Set group prefix", AEPermissionsPerms.GROUP_PREFIX);
            this.valueArg = withRequiredArg("value", "Prefix value", AEStrippedStringArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleSetGroupPrefix(
                    getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)).message());
        }
    }

    private static class AEPermGroupPrefixResetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupPrefixResetCommand() {
            super("reset", "Reset group prefix", AEPermissionsPerms.GROUP_PREFIX);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleResetGroupPrefix(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupSuffixCommand extends AECollectionCommand {

        private AEPermGroupSuffixCommand() {
            super("suffix", "Manage group suffix");
            addSubCommand(new AEPermGroupSuffixGetCommand());
            addSubCommand(new AEPermGroupSuffixSetCommand());
            addSubCommand(new AEPermGroupSuffixResetCommand());
        }
    }

    private static class AEPermGroupSuffixGetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupSuffixGetCommand() {
            super("get", "Get group suffix", AEPermissionsPerms.GROUP_SUFFIX);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleGetGroupSuffix(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupSuffixSetCommand extends AEPermGroupSettingCommand {

        private final RequiredArg<String> valueArg;

        private AEPermGroupSuffixSetCommand() {
            super("set", "Set group suffix", AEPermissionsPerms.GROUP_SUFFIX);
            this.valueArg = withRequiredArg("value", "Suffix value", AEStrippedStringArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleSetGroupSuffix(
                    getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)).message());
        }
    }

    private static class AEPermGroupSuffixResetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupSuffixResetCommand() {
            super("reset", "Reset group suffix", AEPermissionsPerms.GROUP_SUFFIX);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleResetGroupSuffix(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupPriorityCommand extends AECollectionCommand {

        private AEPermGroupPriorityCommand() {
            super("priority", "Manage group priority");
            addSubCommand(new AEPermGroupPriorityGetCommand());
            addSubCommand(new AEPermGroupPrioritySetCommand());
            addSubCommand(new AEPermGroupPriorityResetCommand());
        }
    }

    private static class AEPermGroupPriorityGetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupPriorityGetCommand() {
            super("get", "Get group priority", AEPermissionsPerms.GROUP_PRIORITY);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleGetGroupPriority(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupPrioritySetCommand extends AEPermGroupSettingCommand {

        private final RequiredArg<Integer> valueArg;

        private AEPermGroupPrioritySetCommand() {
            super("set", "Set group priority", AEPermissionsPerms.GROUP_PRIORITY);
            this.valueArg = withRequiredArg("value", "Priority value", ArgTypes.INTEGER);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleSetGroupPriority(
                    getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)).message());
        }
    }

    private static class AEPermGroupPriorityResetCommand extends AEPermGroupSettingCommand {

        private AEPermGroupPriorityResetCommand() {
            super("reset", "Reset group priority", AEPermissionsPerms.GROUP_PRIORITY);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupSettingsModule().handleResetGroupPriority(
                    getExecutionContext(commandContext), groupArg.get(commandContext)));
        }
    }

    private static class AEPermGroupChildrenAddCommand extends AECommand {

        private final RequiredArg<String> groupArg;
        private final RequiredArg<String> childArg;

        /**
         * Creates the group children add command.
         */
        private AEPermGroupChildrenAddCommand() {
            super("add", "Makes a group inherit from another");
            requirePermission(AEPermissionsPerms.GROUP_CHILDREN_ADD);
            this.groupArg = withRequiredArg("group", "Group that will inherit", AEGroupTypeArg.INSTANCE);
            this.childArg = withRequiredArg("child", "Group to inherit from", AEGroupTypeArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            String child = childArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleAddGroupChild(getExecutionContext(commandContext), group, child).message());
        }
    }

    private static class AEPermGroupChildrenRemoveCommand extends AECommand {

        private final RequiredArg<String> groupArg;
        private final RequiredArg<String> childArg;

        /**
         * Creates the group children remove command.
         */
        private AEPermGroupChildrenRemoveCommand() {
            super("remove", "Stops a group from inheriting from another");
            requirePermission(AEPermissionsPerms.GROUP_CHILDREN_REMOVE);
            this.groupArg = withRequiredArg("group", "Group that currently inherits", AEGroupTypeArg.INSTANCE);
            this.childArg = withRequiredArg("child", "Group to stop inheriting from", AEGroupTypeArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            String child = childArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleRemoveGroupChild(getExecutionContext(commandContext), group, child).message());
        }
    }

    private static class AEPermGroupChildrenListCommand extends AECommand {

        private final RequiredArg<String> groupArg;

        /**
         * Creates the group children list command.
         */
        private AEPermGroupChildrenListCommand() {
            super("list", "Lists the groups a group inherits from");
            requirePermission(AEPermissionsPerms.GROUP_CHILDREN_LIST);
            this.groupArg = withRequiredArg("group", "Group name", AEGroupTypeArg.INSTANCE);
        }

        @Override
        protected void executeSync(CommandContext commandContext) {
            String group = groupArg.get(commandContext);
            AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
            reply(commandContext, tcPermissionsModule.getGroupsModule().handleGetGroupChildren(getExecutionContext(commandContext), group));
        }
    }
}
