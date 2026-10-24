package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

public class AEPlayerBreakBlockEvent extends AEEntityBreakBlockEvent {

    private final AEGamePlayer gamePlayer;

    public AEPlayerBreakBlockEvent(Ref<EntityStore> entityStoreRef, BreakBlockEvent event) {
        super(entityStoreRef, event);
        this.gamePlayer = AEGamePlayer.fromEntityStoreRef(entityStoreRef);
    }

    public AEGamePlayer getGamePlayer() {
        return gamePlayer;
    }
}
