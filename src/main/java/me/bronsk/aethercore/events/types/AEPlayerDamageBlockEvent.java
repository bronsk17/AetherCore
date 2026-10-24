package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

public class AEPlayerDamageBlockEvent extends AEEntityDamageBlockEvent {

    private final AEGamePlayer gamePlayer;

    public AEPlayerDamageBlockEvent(Ref<EntityStore> entityStoreRef, DamageBlockEvent event) {
        super(entityStoreRef, event);
        this.gamePlayer = AEGamePlayer.fromEntityStoreRef(entityStoreRef);
    }

    public AEGamePlayer getGamePlayer() {
        return gamePlayer;
    }
}
