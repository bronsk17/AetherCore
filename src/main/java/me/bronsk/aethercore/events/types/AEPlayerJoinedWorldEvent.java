package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;

import java.util.UUID;

public class AEPlayerJoinedWorldEvent extends AEPlayerWorldEvent {

    private final AddPlayerToWorldEvent originalEvent;

    public AEPlayerJoinedWorldEvent(AddPlayerToWorldEvent addPlayerToWorldEvent) {
        super(addPlayerToWorldEvent.getHolder(), addPlayerToWorldEvent.getWorld());
        this.originalEvent = addPlayerToWorldEvent;
    }

    public void setJoinMessage(Message message, boolean broadcastToServer) {
        setJoinMessage(message);
        this.originalEvent.setBroadcastJoinMessage(broadcastToServer);
    }

    public void setJoinMessage(Message message) {
        this.originalEvent.setJoinMessage(message);
    }

    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }

    public UUID getUuid() {
        return getPlayerRef().getUuid();
    }

    public String getUsername() {
        return getPlayerRef().getUsername();
    }
}
