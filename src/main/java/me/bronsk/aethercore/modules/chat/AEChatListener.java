package me.bronsk.aethercore.modules.chat;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.events.bus.AEEventHandler;
import me.bronsk.aethercore.events.types.AEPlayerChatEvent;
import me.bronsk.aethercore.events.types.AEPlayerDisconnectEvent;
import me.bronsk.aethercore.events.types.AEPlayerJoinedWorldEvent;
import me.bronsk.aethercore.events.types.AEPlayerLeftWorldEvent;
import me.bronsk.aethercore.events.types.AEPlayerJoinedServerEvent;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.text.AETextContext;

public class AEChatListener {

    private final AEChatModule chatModule;

    public AEChatListener(AEChatModule chatModule) {
        this.chatModule = chatModule;
    }

    @AEEventHandler(priority = 0)
    public void onPlayerChat(AEPlayerChatEvent event) {
        event.setFormatter((_, _) -> {
            AEGamePlayer gamePlayer = AEGamePlayer.fromUuid(event.getSender().getUuid());
            if (gamePlayer == null) {
                return Message.raw("Error occurred on AetherCore chat module, check console for details");
            }
            return gamePlayer.supplyOnWorld(() -> {
                AETextContext context = new AETextContext(gamePlayer, event.getMessage());
                return chatModule.getChatFormat(context);
            });
        });
    }

    @AEEventHandler
    public void onPlayerJoinedWorld(AEPlayerJoinedWorldEvent event) {
        event.setJoinMessage(null, false);
    }

    @AEEventHandler
    public void onPlayerLeftWorld(AEPlayerLeftWorldEvent event) {
        event.setLeaveMessage(null, false);
    }

    @AEEventHandler
    public void onPlayerJoinedServer(AEPlayerJoinedServerEvent event) {
        AEGamePlayer gamePlayer = event.getGamePlayer();
        gamePlayer.executeOnWorld(() -> {
            AETextContext context = AETextContext.fromPlayer(gamePlayer);
            if (gamePlayer.getPlayer().isFirstSpawn()) {
                Message firstJoinMessage = chatModule.getFirstJoinMessage(context);
                if (firstJoinMessage == null) {
                    return;
                }
                if (chatModule.shouldBroadcastFirstJoinMessage(context)) {
                    Universe.get().sendMessage(firstJoinMessage);
                    return;
                }
                gamePlayer.sendMessage(firstJoinMessage);
                return;
            }

            Message serverJoinMessage = chatModule.getServerJoinMessage(context);
            if (serverJoinMessage != null) {
                Universe.get().sendMessage(serverJoinMessage);
            }

            Message playerJoinMessage = chatModule.getPlayerJoinMessage(context);
            if (playerJoinMessage != null) {
                gamePlayer.sendMessage(playerJoinMessage);
            }
        });
    }

    @AEEventHandler
    public void onPlayerDisconnect(AEPlayerDisconnectEvent event) {
        World world = event.getWorld();
        if (world == null) {
            return;
        }
        world.execute(() -> {
            Message disconnectMessage = chatModule.getLeaveMessage(event.getPlayerRef());
            if (disconnectMessage != null) {
                Universe.get().sendMessage(disconnectMessage);
            }
        });
    }
}
