package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import me.bronsk.aethercore.events.bus.AEBaseCancellableEvent;

public class AEPlayerChatEvent extends AEBaseCancellableEvent {

    private final PlayerChatEvent originalEvent;
    private final String message;
    private final PlayerRef sender;

    /**
     * Creates a new instance.
     *
     * @param playerChatEvent Method argument.
     */
    public AEPlayerChatEvent(PlayerChatEvent playerChatEvent) {
        this.originalEvent = playerChatEvent;
        this.message = playerChatEvent.getContent();
        this.sender = playerChatEvent.getSender();
    }

    public void replyAndCancel(Message reply) {
        this.originalEvent.getSender().sendMessage(reply);
        this.setCancelled(true);
    }

    public void setFormatter(PlayerChatEvent.Formatter formatter) {
        this.originalEvent.setFormatter(formatter);
    }

    /**
     * Returns getOriginalEvent.
     *
     * @return Method result.
     */
    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }


    /**
     * Returns getMessage.
     *
     * @return Method result.
     */
    public String getMessage() {
        return message;
    }

    public PlayerRef getSender() {
        return sender;
    }
}
