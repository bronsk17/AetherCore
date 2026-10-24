package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

public class AEPlayerDamagedByPlayerEvent extends AEPlayerDamagedEvent {

    private final AEGamePlayer attacker;
    private final Ref<EntityStore> attackerRef;
    private final Damage.EntitySource entitySource;

    public AEPlayerDamagedByPlayerEvent(Ref<EntityStore> entityStoreRef, Damage damage, Damage.EntitySource entitySource) {
        super(entityStoreRef, damage);
        this.attackerRef = entitySource.getRef();
        this.attacker = AEGamePlayer.fromEntityStoreRef(attackerRef);
        this.entitySource = entitySource;
    }

    public AEGamePlayer getAttacker() {
        return attacker;
    }

    public Ref<EntityStore> getAttackerRef() {
        return attackerRef;
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
