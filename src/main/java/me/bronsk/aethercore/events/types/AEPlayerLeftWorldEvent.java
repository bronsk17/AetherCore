package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.RemovedPlayerFromWorldEvent;

public class AEPlayerLeftWorldEvent extends AEPlayerWorldEvent {

    private final RemovedPlayerFromWorldEvent originalEvent;

    public AEPlayerLeftWorldEvent(RemovedPlayerFromWorldEvent removedPlayerFromWorldEvent) {
        super(removedPlayerFromWorldEvent.getHolder(), removedPlayerFromWorldEvent.getWorld());
        this.originalEvent = removedPlayerFromWorldEvent;
    }

    public void setLeaveMessage(Message message, boolean broadcastToServer) {
        setLeaveMessage(message);
        this.originalEvent.setBroadcastLeaveMessage(broadcastToServer);
    }

    public void setLeaveMessage(Message message) {
        this.originalEvent.setLeaveMessage(message);
    }

    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }
}
