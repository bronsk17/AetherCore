package me.bronsk.aethercore.modules.player.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.arguments.AEPlayerArg;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.modules.player.AEPlayerPerms;

public class AEKickCommand extends AECommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> reasonArg;

    public AEKickCommand() {
        super("kick", "Kicks a player");
        requirePermission(AEPlayerPerms.KICK);
        this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
        this.reasonArg = withRequiredArg("reason", "Kick reason", AEStrippedStringArg.INSTANCE);
    }

    @Override
    protected void executeSync(CommandContext commandContext) {
        AEPlayersModerationModule tcPlayersModerationModule = AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
        reply(commandContext, tcPlayersModerationModule.handleKick(
                getExecutionContext(commandContext),
                playerArg.get(commandContext),
                reasonArg.get(commandContext)).message());
    }
}
