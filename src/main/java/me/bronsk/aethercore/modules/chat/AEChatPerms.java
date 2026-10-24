package me.bronsk.aethercore.modules.chat;

/**
 * Every permission node owned by the Chat module - generic capability nodes, not tied to how
 * they're invoked (a {@code /aether chat} command today, a UI control tomorrow). Each setting exposed as
 * get/set/reset shares one node across all three verbs.
 */
public final class AEChatPerms {

    /** Allows using color codes in chat instead of having them stripped. */
    public static final String CHAT_COLORIZE = "tc.chat.colorize";

    public static final String GROUP_FORMAT = "tc.chat.group.format";
    public static final String GROUP_SERVER_JOIN_MESSAGE = "tc.chat.group.server_join_message";
    public static final String GROUP_PLAYER_JOIN_MESSAGE = "tc.chat.group.player_join_message";
    public static final String GROUP_LEAVE_MESSAGE = "tc.chat.group.leave_message";
    public static final String GROUP_FIRST_JOIN_MESSAGE = "tc.chat.group.first_join_message";
    public static final String GROUP_BROADCAST_FIRST_JOIN_MESSAGE = "tc.chat.group.broadcast_first_join_message";

    public static final String PLAYER_FORMAT = "tc.chat.player.format";
    public static final String PLAYER_SERVER_JOIN_MESSAGE = "tc.chat.player.server_join_message";
    public static final String PLAYER_PLAYER_JOIN_MESSAGE = "tc.chat.player.player_join_message";
    public static final String PLAYER_LEAVE_MESSAGE = "tc.chat.player.leave_message";
    public static final String PLAYER_FIRST_JOIN_MESSAGE = "tc.chat.player.first_join_message";
    public static final String PLAYER_BROADCAST_FIRST_JOIN_MESSAGE = "tc.chat.player.broadcast_first_join_message";

    private AEChatPerms() {
    }
}
