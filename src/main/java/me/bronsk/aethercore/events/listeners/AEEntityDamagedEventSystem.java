package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEEventBus;
import me.bronsk.aethercore.events.types.AEPlayerDamagedByCommandEvent;
import me.bronsk.aethercore.events.types.AEPlayerDamagedByEntityEvent;
import me.bronsk.aethercore.events.types.AEPlayerDamagedByEnvironmentEvent;
import me.bronsk.aethercore.events.types.AEPlayerDamagedByPlayerEvent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEEntityDamagedEventSystem extends DamageEventSystem {

    protected AEEntityDamagedEventSystem() {
    }

    @Override
    public void handle(int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl Damage damage) {
        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(i);
        Damage.Source source = damage.getSource();
        if (source instanceof Damage.EntitySource entitySource) {
            Ref<EntityStore> sourceRef = entitySource.getRef();
            Player player = sourceRef.getStore().getComponent(sourceRef, Player.getComponentType());
            if (player != null) {
                AEEventBus.get().post(new AEPlayerDamagedByPlayerEvent(entityStoreRef, damage, entitySource));
                return;
            }
            AEEventBus.get().post(new AEPlayerDamagedByEntityEvent(entityStoreRef, damage, entitySource));
            return;
        }
        if (source instanceof Damage.EnvironmentSource environmentSource) {
            AEEventBus.get().post(new AEPlayerDamagedByEnvironmentEvent(entityStoreRef, damage, environmentSource));
            return;
        }
        if (source instanceof Damage.CommandSource commandSource) {
            AEEventBus.get().post(new AEPlayerDamagedByCommandEvent(entityStoreRef, damage, commandSource));
            return;
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
