package me.bronsk.aethercore.modules.player.moderation;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.events.bus.AEEventHandler;
import me.bronsk.aethercore.events.types.AEPlayerChatEvent;
import me.bronsk.aethercore.events.types.AEPlayerDamageBlockEvent;
import me.bronsk.aethercore.events.types.AEPlayerPlaceBlockEvent;
import me.bronsk.aethercore.events.types.AEPlayerSetupConnectEvent;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEExecutionContextFactory;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.components.AEPlayerFreezeComponent;

public class AEPlayersModerationListener {

    private final AEPlayersModerationModule moderationModule;

    public AEPlayersModerationListener(AEPlayersModerationModule moderationModule) {
        this.moderationModule = moderationModule;
    }

    @AEEventHandler
    public void onPlayerSetupConnect(AEPlayerSetupConnectEvent event) {
        AEExecutionContext context = AEExecutionContextFactory.get().fromSystem();
        Message reason = moderationModule.handlePlayerSetupConnect(context, event.getUuid());
        if (reason == null) {
            return;
        }

        event.setReason(reason);
        event.setCancelled(true);
    }

    @AEEventHandler(priority = 9999)
    public void onPlayerChat(AEPlayerChatEvent event) {
        AEExecutionContext context = AEExecutionContextFactory.get().fromUuid(event.getSender().getUuid());
        Message message = moderationModule.handlePlayerChat(context, event.getSender().getUuid());
        if (message == null) { return; }
        event.replyAndCancel(message);
    }

    @AEEventHandler
    public void onPlayerDamageBlock(AEPlayerDamageBlockEvent event) {
        AEGamePlayer tcGamePlayer = event.getGamePlayer();
        tcGamePlayer.executeOnWorld(() -> {
            AEPlayerFreezeComponent tcPlayerFreezeComponent = tcGamePlayer.getComponent(
                    AEPlayerFreezeComponent.getComponentType());
            event.setCancelled(tcPlayerFreezeComponent != null);
        });
    }

    @AEEventHandler
    public void onPlayerPlaceBlock(AEPlayerPlaceBlockEvent event) {
        AEGamePlayer tcGamePlayer = event.getGamePlayer();
        tcGamePlayer.executeOnWorld(() -> {
            AEPlayerFreezeComponent tcPlayerFreezeComponent = tcGamePlayer.getComponent(
                    AEPlayerFreezeComponent.getComponentType());
            event.setCancelled(tcPlayerFreezeComponent != null);
        });
    }
}
