package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEEventBus;
import me.bronsk.aethercore.events.types.AEEntityUseBlockEvent;
import me.bronsk.aethercore.events.types.AEPlayerUseBlockEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEUseBlockEventSystem extends EntityEventSystem<EntityStore, UseBlockEvent> {

    public AEUseBlockEventSystem() {
        super(UseBlockEvent.class);
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl UseBlockEvent useBlockEvent) {
        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(i);
        boolean isPlayer = store.getComponent(entityStoreRef, Player.getComponentType()) != null;
        if (isPlayer) {
            AEEventBus.get().post(new AEPlayerUseBlockEvent(entityStoreRef, useBlockEvent));
        } else {
            AEEventBus.get().post(new AEEntityUseBlockEvent(entityStoreRef, useBlockEvent));
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return AEEntityQuery.INSTANCE;
    }
}
