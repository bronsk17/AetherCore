package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEBaseEvent;

/** Shared world/playerRef resolution for a player entering or leaving a world. */
public abstract class AEPlayerWorldEvent extends AEBaseEvent {

    private final World world;
    private final PlayerRef playerRef;

    protected AEPlayerWorldEvent(Holder<EntityStore> holder, World world) {
        PlayerRef playerRef = holder.getComponent(PlayerRef.getComponentType());
        if (playerRef == null) {
            throw new RuntimeException("PlayerRef is null");
        }
        this.playerRef = playerRef;
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public PlayerRef getPlayerRef() {
        return playerRef;
    }
}
