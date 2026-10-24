package me.bronsk.aethercore.modules.player.models;

import java.sql.Timestamp;

/**
 * One unified row of a player's moderation history - a ban or a mute, already resolved to display
 * values (actor names, computed status) by {@code AEPlayersModule}.
 *
 * @param type "Ban" or "Mute".
 * @param status "Active", "Revoked" or "Expired".
 * @param reason Reason given for the action.
 * @param by Display name of the actor who performed the action, or "-" when unknown.
 * @param revokedBy Display name of the actor who revoked the action, or "-" when not revoked/unknown.
 * @param createdAt When the action was performed.
 * @param expiresAt When the action expires, or null when it never does.
 */
public record AEPlayerModerationHistoryEntry(
        String type,
        String status,
        String reason,
        String by,
        String revokedBy,
        Timestamp createdAt,
        Timestamp expiresAt) {
}
