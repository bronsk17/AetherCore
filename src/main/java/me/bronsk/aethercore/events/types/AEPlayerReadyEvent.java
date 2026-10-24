package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.events.bus.AEBaseEvent;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

public class AEPlayerReadyEvent extends AEBaseEvent {

    private final AEGamePlayer gamePlayer;
    private final PlayerReadyEvent playerReadyEvent;

    /**
     * Creates a new instance.
     *
     * @param event Method argument.
     */
    public AEPlayerReadyEvent(PlayerReadyEvent event) {
        this.playerReadyEvent = event;
        this.gamePlayer = AEGamePlayer.fromEntityStoreRef(event.getPlayerRef());
    }

    /**
     * Returns getGamePlayer.
     *
     * @return Method result.
     */
    public AEGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    /**
     * Returns the world the player is ready in.
     *
     * @return Method result.
     */
    public World getWorld() {
        return gamePlayer.getWorld();
    }

    /**
     * Returns getOriginalEvent.
     *
     * @return Method result.
     */
    @Override
    public Object getOriginalEvent() {
        return playerReadyEvent;
    }
}
