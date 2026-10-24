package me.bronsk.aethercore.modules.player.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.arguments.AEPlayerArg;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.modules.player.AEPlayerPerms;

public class AEUnmuteCommand extends AECommand {

    private final RequiredArg<String> playerArg;

    public AEUnmuteCommand() {
        super("unmute", "Unmutes a player");
        requirePermission(AEPlayerPerms.UNMUTE);
        this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
    }

    @Override
    protected void executeSync(CommandContext commandContext) {
        AEPlayersModerationModule tcPlayersModerationModule = AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
        reply(commandContext, tcPlayersModerationModule.handleUnmute(
                getExecutionContext(commandContext),
                playerArg.get(commandContext)).message());
    }
}
