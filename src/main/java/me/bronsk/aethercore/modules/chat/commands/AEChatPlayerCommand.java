package me.bronsk.aethercore.modules.chat.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import me.bronsk.aethercore.commands.AECollectionCommand;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.commands.arguments.AEBooleanArg;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.chat.AEChatModule;
import me.bronsk.aethercore.modules.chat.AEChatPerms;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.arguments.AEPlayerArg;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@code /aether chat player <setting> get/set/reset <player> [value]} - one collection per chat setting. */
public class AEChatPlayerCommand extends AECollectionCommand {

    public AEChatPlayerCommand() {
        super("player", "Player chat configuration");
        addSubCommand(new AEChatPlayerFormatCommand());
        addSubCommand(new AEChatPlayerServerJoinMessageCommand());
        addSubCommand(new AEChatPlayerPlayerJoinMessageCommand());
        addSubCommand(new AEChatPlayerLeaveMessageCommand());
        addSubCommand(new AEChatPlayerFirstJoinMessageCommand());
        addSubCommand(new AEChatPlayerBroadcastFirstJoinMessageCommand());
    }

    private abstract static class AEChatPlayerSettingCommand extends AECommand {

        final RequiredArg<String> playerArg;

        private AEChatPlayerSettingCommand(String name, String description, String permission) {
            super(name, description);
            requirePermission(permission);
            this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
        }

        static AEChatModule chatModule() {
            return AEModulesManager.get().getModule(AEChatModule.class);
        }

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

    private static class AEChatPlayerFormatCommand extends AECollectionCommand {

        private AEChatPlayerFormatCommand() {
            super("format", "Manage player chat format");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player chat format", AEChatPerms.PLAYER_FORMAT);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerFormat(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set player chat format", AEChatPerms.PLAYER_FORMAT);
                this.valueArg = withRequiredArg("value", "Format value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerFormat(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player chat format", AEChatPerms.PLAYER_FORMAT);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerFormat(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }

    private static class AEChatPlayerServerJoinMessageCommand extends AECollectionCommand {

        private AEChatPlayerServerJoinMessageCommand() {
            super("server_join_message", "Manage player server-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player server-join message", AEChatPerms.PLAYER_SERVER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerServerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set player server-join message", AEChatPerms.PLAYER_SERVER_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerServerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player server-join message", AEChatPerms.PLAYER_SERVER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerServerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }

    private static class AEChatPlayerPlayerJoinMessageCommand extends AECollectionCommand {

        private AEChatPlayerPlayerJoinMessageCommand() {
            super("player_join_message", "Manage player player-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player player-join message", AEChatPerms.PLAYER_PLAYER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerPlayerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set player player-join message", AEChatPerms.PLAYER_PLAYER_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerPlayerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player player-join message", AEChatPerms.PLAYER_PLAYER_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerPlayerJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }

    private static class AEChatPlayerLeaveMessageCommand extends AECollectionCommand {

        private AEChatPlayerLeaveMessageCommand() {
            super("leave_message", "Manage player leave message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player leave message", AEChatPerms.PLAYER_LEAVE_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerLeaveMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set player leave message", AEChatPerms.PLAYER_LEAVE_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerLeaveMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player leave message", AEChatPerms.PLAYER_LEAVE_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerLeaveMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }

    private static class AEChatPlayerFirstJoinMessageCommand extends AECollectionCommand {

        private AEChatPlayerFirstJoinMessageCommand() {
            super("first_join_message", "Manage player first-join message");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player first-join message", AEChatPerms.PLAYER_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<String> valueArg;

            private SetCommand() {
                super("set", "Set player first-join message", AEChatPerms.PLAYER_FIRST_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "Message value", AEStrippedStringArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player first-join message", AEChatPerms.PLAYER_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }

    private static class AEChatPlayerBroadcastFirstJoinMessageCommand extends AECollectionCommand {

        private AEChatPlayerBroadcastFirstJoinMessageCommand() {
            super("broadcast_first_join_message", "Manage whether a player's first join is broadcast server-wide");
            addSubCommand(new GetCommand());
            addSubCommand(new SetCommand());
            addSubCommand(new ResetCommand());
        }

        private static class GetCommand extends AEChatPlayerSettingCommand {
            private GetCommand() {
                super("get", "Get player broadcast-first-join-message", AEChatPerms.PLAYER_BROADCAST_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleGetPlayerBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }

        private static class SetCommand extends AEChatPlayerSettingCommand {
            private final RequiredArg<Boolean> valueArg;

            private SetCommand() {
                super("set", "Set player broadcast-first-join-message", AEChatPerms.PLAYER_BROADCAST_FIRST_JOIN_MESSAGE);
                this.valueArg = withRequiredArg("value", "true or false", AEBooleanArg.INSTANCE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleSetPlayerBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext), valueArg.get(commandContext)));
            }
        }

        private static class ResetCommand extends AEChatPlayerSettingCommand {
            private ResetCommand() {
                super("reset", "Reset player broadcast-first-join-message", AEChatPerms.PLAYER_BROADCAST_FIRST_JOIN_MESSAGE);
            }

            @Override
            protected void executeSync(CommandContext commandContext) {
                reply(commandContext, chatModule().getSettingsModule().handleResetPlayerBroadcastFirstJoinMessage(
                        getExecutionContext(commandContext), playerArg.get(commandContext)));
            }
        }
    }
}
