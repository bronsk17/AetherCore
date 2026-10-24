package me.bronsk.aethercore.events.listeners;

import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.server.core.event.events.player.*;
import me.bronsk.aethercore.events.bus.AEEventBus;
import me.bronsk.aethercore.events.types.*;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AEPlayerEvents {

    private final EventRegistry eventRegistry;

    /** Players between {@link PlayerSetupConnectEvent} and their first {@link PlayerReadyEvent} this session. */
    private final Set<UUID> connectingPlayers = ConcurrentHashMap.newKeySet();

    /**
     * Creates a new instance.
     *
     * @param eventRegistry Method argument.
     */
    public AEPlayerEvents(EventRegistry eventRegistry) {
        this.eventRegistry = eventRegistry;
        registerEvents();
    }

    /**
     * Handles registerEvents.
     */
    private void registerEvents() {
        eventRegistry.registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        eventRegistry.registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        eventRegistry.registerGlobal(PlayerChatEvent.class, this::onPlayerChat);
        eventRegistry.registerGlobal(PlayerSetupConnectEvent.class, this::onPlayerSetupConnect);
        eventRegistry.registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerJoinedWorld);
        eventRegistry.registerGlobal(RemovedPlayerFromWorldEvent.class, this::onPlayerLeftWorld);
    }

    private void onPlayerSetupConnect(PlayerSetupConnectEvent playerSetupConnectEvent) {
        connectingPlayers.add(playerSetupConnectEvent.getUuid());
        AEEventBus.get().post(new AEPlayerSetupConnectEvent(playerSetupConnectEvent));
    }

    private void onPlayerLeftWorld(RemovedPlayerFromWorldEvent removedPlayerFromWorldEvent) {
        AEEventBus.get().post(new AEPlayerLeftWorldEvent(removedPlayerFromWorldEvent));
    }

    private void onPlayerJoinedWorld(AddPlayerToWorldEvent addPlayerToWorldEvent) {
        AEEventBus.get().post(new AEPlayerJoinedWorldEvent(addPlayerToWorldEvent));
    }

    /**
     * Handles onPlayerChat.
     *
     * @param playerChatEvent Method argument.
     */
    private void onPlayerChat(PlayerChatEvent playerChatEvent) {
        AEEventBus.get().post(new AEPlayerChatEvent(playerChatEvent));
    }

    /**
     * Dispatches the player's first ready this session as {@link AEPlayerJoinedServerEvent},
     * and every later one (world switches) as {@link AEPlayerReadyEvent}.
     *
     * @param playerReadyEvent Method argument.
     */
    private void onPlayerReady(PlayerReadyEvent playerReadyEvent) {
        AEPlayerReadyEvent event = new AEPlayerReadyEvent(playerReadyEvent);
        if (connectingPlayers.remove(event.getGamePlayer().getUuid())) {
            AEEventBus.get().post(new AEPlayerJoinedServerEvent(playerReadyEvent, event.getGamePlayer(), event.getWorld()));
        } else {
            AEEventBus.get().post(event);
        }
    }

    /**
     * Handles onPlayerDisconnect.
     *
     * @param playerDisconnectEvent Method argument.
     */
    private void onPlayerDisconnect(PlayerDisconnectEvent playerDisconnectEvent) {
        connectingPlayers.remove(playerDisconnectEvent.getPlayerRef().getUuid());
        AEEventBus.get().post(new AEPlayerDisconnectEvent(playerDisconnectEvent));
    }
}
