package me.bronsk.aethercore.ui.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import me.bronsk.aethercore.commands.AEPlayerCommand;
import me.bronsk.aethercore.ui.framework.AEUIManager;

import java.util.Objects;

/**
 * Root {@code /aether} command - opens the admin interface when invoked bare, and hosts every other
 * module's own command as a subcommand (e.g. {@code /aether chat}, {@code /aether permissions}), see
 * {@link me.bronsk.aethercore.AetherPlugin#getRootCommand()}.
 */
public final class AERootCommand extends AEPlayerCommand {

    private final AEUIManager uiManager;

    public AERootCommand(AEUIManager uiManager) {
        super("aether", "Opens the AetherCore interface");
        this.uiManager = Objects.requireNonNull(uiManager, "uiManager");
    }

    @Override
    protected void executePlayer(CommandContext commandContext, AEPlayerCommandContext playerContext) {
        uiManager.openMain(playerContext.playerRef(), playerContext.store());
    }
}
