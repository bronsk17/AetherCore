package me.bronsk.aethercore.modules.chat.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import me.bronsk.aethercore.commands.AECollectionCommand;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.commands.arguments.AEBooleanArg;
import me.bronsk.aethercore.commands.arguments.AEGroupTypeArg;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.chat.AEChatModule;
import me.bronsk.aethercore.modules.chat.AEChatPerms;

/** {@code /aether chat group <setting> get/set/reset <group> [value]} - one collection per chat setting. */
public class AEChatGroupCommand extends AECollectionCommand {

    public AEChatGroupCommand() {
        super("group", "Group chat configuration");
        addSubCommand(new AEChatGroupFormatCommand());
        addSubCommand(new AEChatGroupServerJoinMessageCommand());
        addSubCommand(new AEChatGroupPlayerJoinMessageCommand());
        addSubCommand(new AEChatGroupLeaveMessageCommand());
        addSubCommand(new AEChatGroupFirstJoinMessageCommand());
        addSubCommand(new AEChatGroupBroadcastFirstJoinMessageCommand());
    }

    private abstract static class AEChatGroupSettingCommand extends AECommand {

        final RequiredArg<String> groupArg;

        private AEChatGroupSettingCommand(String name, String description, String permission) {
            super(name, description);
            requirePermission(permission);
            this.groupArg = withRequiredArg("group", "Group name", AEGroupTypeArg.INSTANCE);
        }

        static AEChatModule chatModule() {
            return AEModulesManager.get().getModule(AEChatModule.class);
        }
    }

    private static class AEChatGroupFormatCommand extends AECollectionCommand {

        private AEChatGroupFormatCommand() {
            super("format", "Manage group chat format");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group chat format", AEChatPerms.GROUP_FORMAT);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupFormat(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set group chat format", AEChatPerms.GROUP_FORMAT);
                this.valueArg = withRequiredArg("value", "Format value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupFormat(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group chat format", AEChatPerms.GROUP_FORMAT);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupFormat(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }

    private static class AEChatGroupServerJoinMessageCommand extends AECollectionCommand {

        private AEChatGroupServerJoinMessageCommand() {
            super("server_join_message", "Manage group server-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group server-join message", AEChatPerms.GROUP_SERVER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupServerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set group server-join message", AEChatPerms.GROUP_SERVER_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupServerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group server-join message", AEChatPerms.GROUP_SERVER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupServerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }

    private static class AEChatGroupPlayerJoinMessageCommand extends AECollectionCommand {

        private AEChatGroupPlayerJoinMessageCommand() {
            super("player_join_message", "Manage group player-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group player-join message", AEChatPerms.GROUP_PLAYER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupPlayerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set group player-join message", AEChatPerms.GROUP_PLAYER_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupPlayerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group player-join message", AEChatPerms.GROUP_PLAYER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupPlayerJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }

    private static class AEChatGroupLeaveMessageCommand extends AECollectionCommand {

        private AEChatGroupLeaveMessageCommand() {
            super("leave_message", "Manage group leave message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group leave message", AEChatPerms.GROUP_LEAVE_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupLeaveMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set group leave message", AEChatPerms.GROUP_LEAVE_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupLeaveMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group leave message", AEChatPerms.GROUP_LEAVE_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupLeaveMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }

    private static class AEChatGroupFirstJoinMessageCommand extends AECollectionCommand {

        private AEChatGroupFirstJoinMessageCommand() {
            super("first_join_message", "Manage group first-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group first-join message", AEChatPerms.GROUP_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set group first-join message", AEChatPerms.GROUP_FIRST_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group first-join message", AEChatPerms.GROUP_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }

    private static class AEChatGroupBroadcastFirstJoinMessageCommand extends AECollectionCommand {

        private AEChatGroupBroadcastFirstJoinMessageCommand() {
            super("broadcast_first_join_message", "Manage whether a group's first join is broadcast server-wide");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatGroupSettingCommand {
            private GetCommand() {
                super("get", "Get group broadcast-first-join-message", AEChatPerms.GROUP_BROADCAST_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetGroupBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatGroupSettingCommand {
            private final RequiredArg<Boolean> valueArg;

            private SetCommand() {
                super("set", "Set group broadcast-first-join-message", AEChatPerms.GROUP_BROADCAST_FIRST_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "true or false", AEBooleanArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetGroupBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatGroupSettingCommand {
            private ResetCommand() {
                super("reset", "Reset group broadcast-first-join-message", AEChatPerms.GROUP_BROADCAST_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetGroupBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), groupArg.get(commandContext)));
            }
        }
    }
}
