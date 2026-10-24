package me.bronsk.aethercore.modules.player;

import me.bronsk.aethercore.events.bus.AEEventHandler;
import me.bronsk.aethercore.events.types.AEPlayerJoinedServerEvent;

public class AEPlayerListener {

    private final AEPlayersModule playersModule;

    public AEPlayerListener(AEPlayersModule playersModule) {
        this.playersModule = playersModule;
    }

    /**
     * Handles the player joining the server.
     *
     * @param event Player joined-server event.
     */
    @AEEventHandler
    public void onPlayerJoinedServer(AEPlayerJoinedServerEvent event) {
        playersModule.handleJoin(event.getGamePlayer());
    }
}
