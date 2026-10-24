package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AEPlayerDamagedByEnvironmentEvent extends AEPlayerDamagedEvent {

    private final Damage.EnvironmentSource environmentSource;

    public AEPlayerDamagedByEnvironmentEvent(Ref<EntityStore> entityStoreRef, Damage damage, Damage.EnvironmentSource environmentSource) {
        super(entityStoreRef, damage);
        this.environmentSource = environmentSource;
    }

    public Damage.EnvironmentSource getEnvironmentSource() {
        return environmentSource;
    }

    public String getType() {
        return environmentSource.getType();
    }
}
