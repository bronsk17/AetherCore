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

public class AEBanCommand extends AECommand {

    private final RequiredArg<String> playerArg;
    private final RequiredArg<String> reasonArg;

    public AEBanCommand() {
        super("ban", "Bans a player");
        requirePermission(AEPlayerPerms.BAN);
        this.playerArg = withRequiredArg("player", "UUID or username", AEPlayerArg.INSTANCE);
        this.reasonArg = withRequiredArg("reason", "Ban reason", AEStrippedStringArg.INSTANCE);
    }

    @Override
    protected void executeSync(CommandContext commandContext) {
        AEPlayersModerationModule tcPlayersModerationModule = AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
        reply(commandContext, tcPlayersModerationModule.handleBan(
                getExecutionContext(commandContext),
                playerArg.get(commandContext),
                reasonArg.get(commandContext)).message());
    }
}
