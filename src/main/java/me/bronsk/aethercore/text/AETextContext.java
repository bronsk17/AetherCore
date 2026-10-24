package me.bronsk.aethercore.text;

import me.bronsk.aethercore.modules.player.AEGamePlayer;

public record AETextContext(AEGamePlayer player, String rawMessage) {

    public static AETextContext fromPlayer(AEGamePlayer player) {
        return new AETextContext(player, null);
    }

    public AETextContext withRawMessage(String rawMessage) {
        return new AETextContext(player, rawMessage);
    }

    public boolean hasRawMessage() {
        return rawMessage != null;
    }
}
