package me.bronsk.aethercore.modules.player;

/** Every permission node owned by the Player module (moderation actions) - generic capability nodes, not tied to how they're invoked. */
public final class AEPlayerPerms {

    public static final String BAN = "tc.player.ban";
    public static final String TEMPBAN = "tc.player.tempban";
    public static final String MUTE = "tc.player.mute";
    public static final String TEMPMUTE = "tc.player.tempmute";
    public static final String UNBAN = "tc.player.unban";
    public static final String UNMUTE = "tc.player.unmute";
    public static final String FREEZE = "tc.player.freeze";
    public static final String UNFREEZE = "tc.player.unfreeze";
    public static final String KICK = "tc.player.kick";
    public static final String INVENTORY_MODIFY = "tc.player.inventory.modify";

    private AEPlayerPerms() {
    }
}
