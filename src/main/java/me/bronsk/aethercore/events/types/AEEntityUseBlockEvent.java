package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

public class AEEntityUseBlockEvent extends AEEntityBlockEvent<UseBlockEvent> {

    public AEEntityUseBlockEvent(Ref<EntityStore> entityStoreRef, UseBlockEvent event) {
        super(entityStoreRef, event);
    }

    public BlockType getBlockType() {
        return getOriginalEvent().getBlockType();
    }

    public Vector3i getTargetBlock() {
        return getOriginalEvent().getTargetBlock();
    }

    public InteractionType getInteractionType() {
        return getOriginalEvent().getInteractionType();
    }

    public InteractionContext getContext() {
        return getOriginalEvent().getContext();
    }
}
