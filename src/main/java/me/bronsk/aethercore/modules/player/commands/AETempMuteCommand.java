package me.bronsk.aethercore.modules.player.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.commands.arguments.AEStrippedStringArg;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.arguments.AEPlayerArg;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.modules.player.AEPlayerPerms;

import java.time.Duration;

public class AETempMuteCommand extends AECommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<Duration> durationArg;
    private final RequiredArg<String> reasonArg;

    public AETempMuteCommand() {
        super("tempmute", "Temporarily mutes a player");
        requirePermission(AEPlayerPerms.TEMPMUTE);
        this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
        this.durationArg = withRequiredArg("duration", "Duration like 10m, 2h, 7d", AEDurationArg.INSTANCE);
        this.reasonArg = withRequiredArg("reason", "Mute reason", AEStrippedStringArg.INSTANCE);
    }

    @Override
    protected void executeSync(CommandContext commandContext) {
        AEPlayersModerationModule tcPlayersModerationModule = AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
        reply(commandContext, tcPlayersModerationModule.handleTempMute(
                getExecutionContext(commandContext),
                playerArg.get(commandContext),
                durationArg.get(commandContext),
                reasonArg.get(commandContext)).message());
    }
}
