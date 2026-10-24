package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.events.bus.AEBaseEvent;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

import java.util.UUID;

public class AEPlayerDisconnectEvent extends AEBaseEvent {

    private final PlayerRef playerRef;
    private final PlayerDisconnectEvent originalEvent;


    public AEPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        this.originalEvent = event;
        this.playerRef = event.getPlayerRef();
    }

    public PlayerRef getPlayerRef() {
        return playerRef;
    }

    public World getWorld() {
        UUID worldUuid = playerRef.getWorldUuid();
        if (worldUuid == null) {
            return null;
        }
        return Universe.get().getWorld(worldUuid);
    }

    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }
}
