package me.bronsk.aethercore.modules.player.systems;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.components.AEPlayerFreezeComponent;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;

public class AEPlayerFreezeChangeSystem extends RefChangeSystem<EntityStore, AEPlayerFreezeComponent> {

    private final AEPlayerFreezeSystem tcPlayerFreezeSystem;

    public AEPlayerFreezeChangeSystem(@Nonnull AEPlayerFreezeSystem tcPlayerFreezeSystem) {
        this.tcPlayerFreezeSystem = tcPlayerFreezeSystem;
    }

    @NonNullDecl
    @Override
    public ComponentType<EntityStore, AEPlayerFreezeComponent> componentType() {
        return AEPlayerFreezeComponent.getComponentType();
    }

    @Override
    public void onComponentAdded(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AEPlayerFreezeComponent tcPlayerFreezeComponent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

    }

    @Override
    public void onComponentSet(@NonNullDecl Ref<EntityStore> ref, @NullableDecl AEPlayerFreezeComponent tcPlayerFreezeComponent, @NonNullDecl AEPlayerFreezeComponent t1, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {

    }

    @Override
    public void onComponentRemoved(@NonNullDecl Ref<EntityStore> ref, @NonNullDecl AEPlayerFreezeComponent tcPlayerFreezeComponent, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer) {
        AEGamePlayer tcGamePlayer = AEGamePlayer.fromEntityStoreRef(ref);
        tcPlayerFreezeSystem.unfreeze(tcGamePlayer, tcPlayerFreezeComponent);
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return tcPlayerFreezeSystem.getQuery();
    }
}
