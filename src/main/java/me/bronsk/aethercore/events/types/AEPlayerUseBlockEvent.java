package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

public class AEPlayerUseBlockEvent extends AEEntityUseBlockEvent {

    private final AEGamePlayer gamePlayer;

    public AEPlayerUseBlockEvent(Ref<EntityStore> entityStoreRef, UseBlockEvent event) {
        super(entityStoreRef, event);
        this.gamePlayer = AEGamePlayer.fromEntityStoreRef(entityStoreRef);
    }

    public AEGamePlayer getGamePlayer() {
        return gamePlayer;
    }
}
