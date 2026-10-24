package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.utils.AEBlockUtils;
import org.joml.Vector3i;

public class AEEntityPlaceBlockEvent extends AEEntityBlockEvent<PlaceBlockEvent> {

    private final BlockType blockType;

    public AEEntityPlaceBlockEvent(Ref<EntityStore> entityStoreRef, PlaceBlockEvent event) {
        super(entityStoreRef, event);
        ItemStack itemPlaced = event.getItemInHand();
        this.blockType = itemPlaced == null ? null : AEBlockUtils.getBlockTypeFromItemStack(itemPlaced);
    }

    public Vector3i getPosition() {
        return getOriginalEvent().getTargetBlock();
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public ItemStack getItemInHand() {
        return getOriginalEvent().getItemInHand();
    }

    public RotationTuple getRotation() {
        return getOriginalEvent().getRotation();
    }

    public void setRotation(RotationTuple rotation) {
        getOriginalEvent().setRotation(rotation);
    }
}
