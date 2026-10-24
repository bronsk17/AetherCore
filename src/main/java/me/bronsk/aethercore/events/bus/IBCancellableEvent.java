package me.bronsk.aethercore.events.bus;

public interface IBCancellableEvent {
    boolean isCancelled();
    void setCancelled(boolean isCancelled);
}
