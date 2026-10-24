package me.bronsk.aethercore.modules.player.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.modules.entity.component.CollisionResultComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.components.AEPlayerFreezeComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEPlayerFreezeSystem extends EntityTickingSystem<EntityStore> {

    private final ComponentType<EntityStore, AEPlayerFreezeComponent> componentType;

    public AEPlayerFreezeSystem() {
        this.componentType = registerComponent(AEPlayerFreezeComponent.class, AEPlayerFreezeComponent::new);
        AEPlayerFreezeComponent.setComponentType(this.componentType);
    }

    @Override
    public void tick(float v, int i, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        AEPlayerFreezeComponent tcPlayerFreezeComponent = store.getComponent(ref, componentType);
        if (tcPlayerFreezeComponent == null) {
            return;
        }
        AEGamePlayer tcGamePlayer = AEGamePlayer.fromEntityStoreRef(ref);
        freeze(tcGamePlayer, tcPlayerFreezeComponent);
    }


    public void freeze(AEGamePlayer tcGamePlayer, AEPlayerFreezeComponent tcPlayerFreezeComponent) {
        MovementManager movementManager = tcGamePlayer.getComponent(MovementManager.getComponentType());
        PlayerRef playerRef = tcGamePlayer.getComponent(PlayerRef.getComponentType());
        if (movementManager == null || movementManager.getSettings() == null) {
            return;
        }
        MovementSettings movementSettings = movementManager.getSettings();
        tcPlayerFreezeComponent.savePreviousSpeed(movementSettings.baseSpeed);
        if (movementSettings.baseSpeed == 0f) {
            return;
        }
        movementSettings.baseSpeed = 0f;
        if (playerRef != null) {
            movementManager.update(playerRef.getPacketHandler());
        }
        CollisionResultComponent collisionResultComponent = tcGamePlayer.getComponent(
                CollisionResultComponent.getComponentType());
        if (collisionResultComponent != null) {
            collisionResultComponent.getCollisionResult().disableTriggerBlocks();
        }
    }

    public void unfreeze(AEGamePlayer tcGamePlayer, AEPlayerFreezeComponent tcPlayerFreezeComponent) {
        MovementManager movementManager = tcGamePlayer.getComponent(MovementManager.getComponentType());
        PlayerRef playerRef = tcGamePlayer.getComponent(PlayerRef.getComponentType());
        if (movementManager == null || movementManager.getSettings() == null) {
            return;
        }
        MovementSettings movementSettings = movementManager.getSettings();
        movementSettings.baseSpeed = tcPlayerFreezeComponent.getPreviousBaseSpeed();
        if (playerRef != null) {
            movementManager.update(playerRef.getPacketHandler());
        }
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }
}
