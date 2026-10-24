package me.bronsk.aethercore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

import java.util.concurrent.CompletableFuture;

public abstract class AEPlayerCommand extends AECommand {

    /**
     * Creates a player-only command.
     *
     * @param name Command name.
     * @param description Command description.
     */
    protected AEPlayerCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Creates a player-only command with explicit confirmation behavior.
     *
     * @param name Command name.
     * @param description Command description.
     * @param requireConfirmation True when the command requires a trailing --confirm.
     */
    protected AEPlayerCommand(String name, String description, boolean requireConfirmation) {
        super(name, description, requireConfirmation);
    }

    /**
     * Creates a player-only usage variant using the SDK default name handling.
     *
     * @param description Command description.
     */
    protected AEPlayerCommand(String description) {
        super(description);
    }

    /**
     * Resolves the player sender and executes the player hook.
     *
     * @param commandContext Command execution context.
     * @return Completion future for the command execution.
     */
    @Override
    protected final CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        return executeOnPlayerWorld(commandContext, playerContext -> executePlayerAsync(commandContext, playerContext));
    }

    /**
     * Executes a player command asynchronously.
     *
     * @param commandContext Command execution context.
     * @param playerContext Resolved player command context.
     * @return Completion future for the command execution.
     */
    protected CompletableFuture<Void> executePlayerAsync(CommandContext commandContext, AEPlayerCommandContext playerContext) {
        executePlayer(commandContext, playerContext);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Executes a player command synchronously.
     *
     * @param commandContext Command execution context.
     * @param playerContext Resolved player command context.
     */
    protected void executePlayer(CommandContext commandContext, AEPlayerCommandContext playerContext) {
        replyUsage(commandContext);
    }

    public record AEPlayerCommandContext(
            Store<EntityStore> store,
            Ref<EntityStore> ref,
            AEGamePlayer player,
            PlayerRef playerRef,
            World world) {
    }
}
