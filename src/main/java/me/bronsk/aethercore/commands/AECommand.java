package me.bronsk.aethercore.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEExecutionContextFactory;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AECommand extends AbstractCommand {

    /**
     * Creates a AetherCore command.
     *
     * @param name Command name.
     * @param description Command description.
     */
    protected AECommand(String name, String description) {
        super(name, description);
        setAllowsExtraArguments(true);
    }

    /**
     * Creates a AetherCore command with explicit confirmation behavior.
     *
     * @param name Command name.
     * @param description Command description.
     * @param requireConfirmation True when the command requires a trailing --confirm.
     */
    protected AECommand(String name, String description, boolean requireConfirmation) {
        super(name, description, requireConfirmation);
        setAllowsExtraArguments(true);
    }

    /**
     * Creates a AetherCore command using the SDK default description.
     *
     * @param name Command name.
     */
    protected AECommand(String name) {
        super(name);
        setAllowsExtraArguments(true);
    }

    /**
     * Routes every SDK command execution through the AetherCore async hook.
     *
     * @param commandContext Command execution context.
     * @return Completion future for the command execution.
     */
    @Override
    protected final CompletableFuture<Void> execute(CommandContext commandContext) {
        try {
            return executeAsync(commandContext).exceptionally(throwable -> {
                handleExecutionFailure(commandContext, throwable);
                return null;
            });
        } catch (Exception exception) {
            handleExecutionFailure(commandContext, exception);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Logs an unhandled command execution failure and notifies the sender.
     *
     * @param commandContext Command execution context.
     * @param throwable Failure raised during execution.
     */
    private void handleExecutionFailure(CommandContext commandContext, Throwable throwable) {
        AELogger.logException("Command '" + getName() + "' failed", throwable);
        reply(commandContext, AEMessageUtils.fromColoredString("&cAn internal error occurred while executing this command."));
    }

    /**
     * Executes the command asynchronously.
     *
     * @param commandContext Command execution context.
     * @return Completion future for the command execution.
     */
    protected CompletableFuture<Void> executeAsync(CommandContext commandContext) {
        executeSync(commandContext);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Executes the command synchronously.
     *
     * @param commandContext Command execution context.
     */
    protected void executeSync(CommandContext commandContext) {
        replyUsage(commandContext);
    }

    /**
     * Sends a command response to players or logs it for non-player senders.
     *
     * @param commandContext Command execution context.
     * @param message Message to send.
     */
    protected void reply(CommandContext commandContext, Message message) {
        commandContext.sendMessage(message);
    }

    /**
     * Sends this command's usage to the caller.
     *
     * @param commandContext Command execution context.
     */
    protected void replyUsage(CommandContext commandContext) {
        reply(commandContext, getUsageString(commandContext.sender()));
    }

    /**
     * Executes a player operation on the sender's world thread before reading player components.
     *
     * @param commandContext Command execution context.
     * @param action Action that receives the resolved player context.
     * @return Completion future for the world-thread action.
     */
    protected CompletableFuture<Void> executeOnPlayerWorld(CommandContext commandContext, Function<AEPlayerCommand.AEPlayerCommandContext, CompletableFuture<Void>> action) {
        Ref<EntityStore> ref = commandContext.senderAsPlayerRef();
        if (ref == null) {
            reply(commandContext, Message.translation("server.commands.errors.playerOrArg").param("option", "player"));
            return CompletableFuture.completedFuture(null);
        }

        if (!ref.isValid()) {
            reply(commandContext, Message.translation("server.commands.errors.playerNotInWorld"));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        AEGamePlayer player = AEGamePlayer.fromEntityStoreRef(ref);
        return player.executeOnWorld(() -> {
            AEPlayerCommand.AEPlayerCommandContext playerContext = new AEPlayerCommand.AEPlayerCommandContext(store, ref, player, player.getPlayerRef(), world);
            action.apply(playerContext).join();
        });
    }

    /**
     * Executes an action on the requested world thread.
     *
     * @param commandContext Command execution context.
     * @param worldName Target world name.
     * @param action Action that receives the loaded target world.
     * @return Completion future for the action.
     */
    protected CompletableFuture<Void> executeOnWorld(CommandContext commandContext, String worldName,
                                                     Function<World, CompletableFuture<Void>> action) {
        World world = Universe.get().getWorld(worldName);
        if (world == null) {
            reply(commandContext, AEMessageUtils.fromColoredString("&7World &6" + worldName + "&7 is not loaded"));
            return CompletableFuture.completedFuture(null);
        }

        if (world.isInThread()) {
            return action.apply(world);
        }

        return CompletableFuture.runAsync(() -> action.apply(world).join(), world);
    }

    /**
     * Executes an action on the target player's world thread when that player is online.
     *
     * @param playerUuid Target player UUID.
     * @param action Action to run.
     * @return Completion future for the action.
     */
    protected CompletableFuture<Void> executeOnOnlinePlayerWorld(UUID playerUuid, Runnable action) {
        AEGamePlayer player = AEGamePlayer.fromUuid(playerUuid);
        if (player == null) {
            action.run();
            return CompletableFuture.completedFuture(null);
        }

        return player.executeOnWorld(action);
    }

    protected static AEExecutionContext getExecutionContext(CommandContext commandContext) {
        if (!commandContext.isPlayer()) {
            return AEExecutionContextFactory.get().fromConsole();
        }
        return AEExecutionContextFactory.get().fromUuid(commandContext.sender().getUuid());
    }
}
