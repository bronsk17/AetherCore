package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEEventBus;
import me.bronsk.aethercore.events.types.AEEntityBreakBlockEvent;
import me.bronsk.aethercore.events.types.AEPlayerBreakBlockEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEBreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    /**
     * Creates a new instance.
     */
    public AEBreakBlockEventSystem() {
        super(BreakBlockEvent.class);
    }

    /**
     * Handles handle.
     *
     * @param i Method argument.
     * @param archetypeChunk Method argument.
     * @param store Method argument.
     * @param commandBuffer Method argument.
     * @param breakBlockEvent Method argument.
     */
    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl BreakBlockEvent breakBlockEvent) {
        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(i);
        boolean isPlayer = store.getComponent(entityStoreRef, Player.getComponentType()) != null;
        if (isPlayer) {
            AEEventBus.get().post(new AEPlayerBreakBlockEvent(entityStoreRef, breakBlockEvent));
        } else {
            AEEventBus.get().post(new AEEntityBreakBlockEvent(entityStoreRef, breakBlockEvent));
        }
    }

    /**
     * Returns getQuery.
     *
     * @return Method result.
     */
    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return AEEntityQuery.INSTANCE;
    }
}
