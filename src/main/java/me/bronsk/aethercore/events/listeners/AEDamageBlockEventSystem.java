package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEEventBus;
import me.bronsk.aethercore.events.types.AEEntityDamageBlockEvent;
import me.bronsk.aethercore.events.types.AEPlayerDamageBlockEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEDamageBlockEventSystem extends EntityEventSystem<EntityStore, DamageBlockEvent> {

    public AEDamageBlockEventSystem() {
        super(DamageBlockEvent.class);
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk,
                       @NonNullDecl Store<EntityStore> store,
                       @NonNullDecl CommandBuffer<EntityStore> commandBuffer,
                       @NonNullDecl DamageBlockEvent damageBlockEvent) {
        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(i);
        boolean isPlayer = store.getComponent(entityStoreRef, Player.getComponentType()) != null;
        if (isPlayer) {
            AEEventBus.get().post(new AEPlayerDamageBlockEvent(entityStoreRef, damageBlockEvent));
        } else {
            AEEventBus.get().post(new AEEntityDamageBlockEvent(entityStoreRef, damageBlockEvent));
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return AEEntityQuery.INSTANCE;
    }
}
