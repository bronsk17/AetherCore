package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

public class AEEntityDamageBlockEvent extends AEEntityBlockEvent<DamageBlockEvent> {

    public AEEntityDamageBlockEvent(Ref<EntityStore> entityStoreRef, DamageBlockEvent event) {
        super(entityStoreRef, event);
    }

    public ItemStack getItemInHand() {
        return getOriginalEvent().getItemInHand();
    }

    public Vector3i getTargetBlock() {
        return getOriginalEvent().getTargetBlock();
    }

    public BlockType getBlockType() {
        return getOriginalEvent().getBlockType();
    }

    public float getCurrentDamage() {
        return getOriginalEvent().getCurrentDamage();
    }

    public float getDamage() {
        return getOriginalEvent().getDamage();
    }

    public void setDamage(float damage) {
        getOriginalEvent().setDamage(damage);
    }
}
