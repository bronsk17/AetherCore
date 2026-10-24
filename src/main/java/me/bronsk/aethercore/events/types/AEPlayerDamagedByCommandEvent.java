package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AEPlayerDamagedByCommandEvent extends AEPlayerDamagedEvent {

    private final Damage.CommandSource commandSource;

    public AEPlayerDamagedByCommandEvent(Ref<EntityStore> entityStoreRef, Damage damage, Damage.CommandSource commandSource) {
        super(entityStoreRef, damage);
        this.commandSource = commandSource;
    }

    public Damage.CommandSource getCommandSource() {
        return commandSource;
    }
}
