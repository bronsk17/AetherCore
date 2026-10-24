package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

public class AEEntityBreakBlockEvent extends AEEntityBlockEvent<BreakBlockEvent> {

    public AEEntityBreakBlockEvent(Ref<EntityStore> entityStoreRef, BreakBlockEvent event) {
        super(entityStoreRef, event);
    }

    public Vector3i getPosition() {
        return getOriginalEvent().getTargetBlock();
    }

    public BlockType getBlockType() {
        return getOriginalEvent().getBlockType();
    }

    public ItemStack getItemInHand() {
        return getOriginalEvent().getItemInHand();
    }
}
