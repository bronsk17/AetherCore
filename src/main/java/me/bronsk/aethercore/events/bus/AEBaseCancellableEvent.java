package me.bronsk.aethercore.events.bus;

import com.hypixel.hytale.component.system.CancellableEcsEvent;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import com.hypixel.hytale.event.ICancellable;
import me.bronsk.aethercore.AELogger;

public abstract class AEBaseCancellableEvent extends AEBaseEvent implements IBCancellableEvent {

    private boolean isCancelled;

    /**
     * Returns isCancelled.
     *
     * @return Method result.
     */
    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    /**
     * Sets setCancelled.
     *
     * @param isCancelled Method argument.
     */
    @Override
    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
        tryCancelICancellableEcsEvent();
        tryCancelICancellable();
    }

    private void tryCancelICancellableEcsEvent() {
        try {
            ICancellableEcsEvent originalEvent = (ICancellableEcsEvent) getOriginalEvent();
            originalEvent.setCancelled(isCancelled);
        } catch (ClassCastException | NullPointerException _) {}
    }

    private void tryCancelICancellable() {
        try {
            ICancellable originalEvent = (ICancellable) getOriginalEvent();
            if (originalEvent == null) { return; }
            originalEvent.setCancelled(isCancelled);
        } catch (ClassCastException | NullPointerException _) {}
    }
}
