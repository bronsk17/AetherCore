package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.events.bus.AEBaseEvent;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

/**
 * Fired once per connection: the first {@link AEPlayerReadyEvent} after the player's
 * {@link AEPlayerSetupConnectEvent}, i.e. the player has joined the server and is fully
 * initialized in their first world. Later world switches fire {@link AEPlayerReadyEvent} instead.
 */
public class AEPlayerJoinedServerEvent extends AEBaseEvent {

    private final PlayerReadyEvent originalEvent;
    private final AEGamePlayer gamePlayer;
    private final World world;

    public AEPlayerJoinedServerEvent(PlayerReadyEvent originalEvent, AEGamePlayer gamePlayer, World world) {
        this.originalEvent = originalEvent;
        this.gamePlayer = gamePlayer;
        this.world = world;
    }

    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }

    public AEGamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public World getWorld() {
        return world;
    }
}
