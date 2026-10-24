package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.events.bus.AEBaseCancellableEvent;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

/** Shared victim/damage fields for a player taking damage, regardless of source. */
public abstract class AEPlayerDamagedEvent extends AEBaseCancellableEvent {

    private final AEGamePlayer victim;
    private final Damage damage;

    protected AEPlayerDamagedEvent(Ref<EntityStore> entityStoreRef, Damage damage) {
        this.victim = AEGamePlayer.fromEntityStoreRef(entityStoreRef);
        this.damage = damage;
    }

    public AEGamePlayer getVictim() {
        return victim;
    }

    public Damage getDamage() {
        return damage;
    }

    public float getAmount() {
        return damage.getAmount();
    }

    public void setAmount(float amount) {
        damage.setAmount(amount);
    }

    public float getInitialAmount() {
        return damage.getInitialAmount();
    }

    public DamageCause getCause() {
        return damage.getCause();
    }

    @Override
    public Object getOriginalEvent() {
        return damage;
    }
}
