package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class AEPlayerDamagedByEntityEvent extends AEPlayerDamagedEvent {

    private final Ref<EntityStore> attacker;
    private final Damage.EntitySource entitySource;

    public AEPlayerDamagedByEntityEvent(Ref<EntityStore> entityStoreRef, Damage damage, Damage.EntitySource entitySource) {
        super(entityStoreRef, damage);
        this.attacker = entitySource.getRef();
        this.entitySource = entitySource;
    }

    public Ref<EntityStore> getAttacker() {
        return attacker;
    }

    public Damage.EntitySource getEntitySource() {
        return entitySource;
    }

    public boolean isProjectile() {
        return entitySource instanceof Damage.ProjectileSource;
    }

    public Ref<EntityStore> getProjectile() {
        if (entitySource instanceof Damage.ProjectileSource projectileSource) {
            return projectileSource.getProjectile();
        }
        return null;
    }
}
