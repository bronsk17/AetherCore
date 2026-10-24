package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEBaseCancellableEvent;

/** Shared entity/world/original-event fields for a non-player entity interacting with a block. */
public abstract class AEEntityBlockEvent<T> extends AEBaseCancellableEvent {

    private final Ref<EntityStore> entityStoreRef;
    private final T originalEvent;
    private final World world;

    protected AEEntityBlockEvent(Ref<EntityStore> entityStoreRef, T originalEvent) {
        this.entityStoreRef = entityStoreRef;
        this.originalEvent = originalEvent;
        this.world = entityStoreRef.getStore().getExternalData().getWorld();
    }

    public Ref<EntityStore> getEntityStoreRef() {
        return entityStoreRef;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public T getOriginalEvent() {
        return originalEvent;
    }
}
