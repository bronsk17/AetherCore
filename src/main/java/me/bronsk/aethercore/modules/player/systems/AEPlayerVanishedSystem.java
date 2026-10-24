package me.bronsk.aethercore.modules.player.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.components.AEPlayerVanishedComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/**
 * Only exists to register {@link AEPlayerVanishedComponent}'s type on the entity store - vanish
 * itself is a one-shot action ({@code AEGamePlayer#setIsVanished}) with nothing to do every tick,
 * unlike {@code AEPlayerFreezeSystem} (which continuously re-applies zero speed). Same registration
 * shape regardless, so the component can be added/removed via the normal {@code Store} API.
 */
public class AEPlayerVanishedSystem extends EntityTickingSystem<EntityStore> {

    public AEPlayerVanishedSystem() {
        ComponentType<EntityStore, AEPlayerVanishedComponent> componentType =
                registerComponent(AEPlayerVanishedComponent.class, AEPlayerVanishedComponent::new);
        AEPlayerVanishedComponent.setComponentType(componentType);
    }

    @Override
    public void tick(float delta, int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
