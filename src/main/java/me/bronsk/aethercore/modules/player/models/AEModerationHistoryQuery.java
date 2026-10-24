package me.bronsk.aethercore.modules.player.models;

/**
 * Describes one deterministic page of a player's moderation history (bans + mutes merged).
 *
 * @param playerUuid Player whose history is requested.
 * @param pageIndex Zero-based page index.
 * @param pageSize Number of entries requested for the page.
 * @param search Case-insensitive filter over type/reason/actor/status.
 */
public record AEModerationHistoryQuery(String playerUuid, int pageIndex, int pageSize, String search) {

    public AEModerationHistoryQuery {
        if (playerUuid == null || playerUuid.isBlank()) {
            throw new IllegalArgumentException("playerUuid must not be blank");
        }
        if (pageIndex < 0) {
            throw new IllegalArgumentException("pageIndex must be greater than or equal to zero");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        search = search == null ? "" : search.strip();
    }

    public AEModerationHistoryQuery(String playerUuid, int pageIndex, int pageSize) {
        this(playerUuid, pageIndex, pageSize, "");
    }

    public long offset() {
        return (long) pageIndex * pageSize;
    }

    public boolean hasSearch() {
        return !search.isEmpty();
    }
}
