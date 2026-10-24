package me.bronsk.aethercore.events.types;

import com.hypixel.hytale.protocol.HostAddress;
import com.hypixel.hytale.protocol.packets.auth.ClientReferral;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.auth.PlayerAuthentication;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.io.PacketHandler;
import me.bronsk.aethercore.events.bus.AEBaseCancellableEvent;

import java.util.UUID;

public class AEPlayerSetupConnectEvent extends AEBaseCancellableEvent {

    private final PlayerSetupConnectEvent originalEvent;

    /**
     * Creates a new instance.
     *
     * @param event Method argument.
     */
    public AEPlayerSetupConnectEvent(PlayerSetupConnectEvent event) {
        this.originalEvent = event;
    }

    @Override
    public Object getOriginalEvent() {
        return originalEvent;
    }

    @Override
    public boolean isCancelled() {
        return originalEvent.isCancelled();
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        super.setCancelled(isCancelled);
        originalEvent.setCancelled(isCancelled);
    }

    public PacketHandler getPacketHandler() {
        return originalEvent.getPacketHandler();
    }

    public UUID getUuid() {
        return originalEvent.getUuid();
    }

    public String getUsername() {
        return originalEvent.getUsername();
    }

    public PlayerAuthentication getAuth() {
        return originalEvent.getAuth();
    }

    public byte[] getReferralData() {
        return originalEvent.getReferralData();
    }

    public boolean isReferralConnection() {
        return originalEvent.isReferralConnection();
    }

    public HostAddress getReferralSource() {
        return originalEvent.getReferralSource();
    }

    public ClientReferral getClientReferral() {
        return originalEvent.getClientReferral();
    }

    public void referToServer(String host, int port) {
        originalEvent.referToServer(host, port);
    }

    public void referToServer(String host, int port, byte[] data) {
        originalEvent.referToServer(host, port, data);
    }

    public Message getReason() {
        return originalEvent.getReason();
    }

    public void setReason() {
        setReason(PlayerSetupConnectEvent.DEFAULT_REASON);
    }

    public void setReason(Message reason) {
        originalEvent.setReason(reason);
    }
}
