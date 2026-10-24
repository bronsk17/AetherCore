package me.bronsk.aethercore.modules.player.models;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result of a {@link AEModerationHistoryQuery}.
 *
 * @param query Query used to produce this page.
 * @param entries Entries contained in the requested page.
 * @param totalEntries Total number of entries matching the query's search filter.
 */
public record AEModerationHistoryResponse(
        AEModerationHistoryQuery query,
        List<AEPlayerModerationHistoryEntry> entries,
        long totalEntries) {

    public AEModerationHistoryResponse {
        Objects.requireNonNull(query, "query");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        if (totalEntries < 0) {
            throw new IllegalArgumentException("totalEntries must be greater than or equal to zero");
        }
    }

    public int totalPages() {
        if (totalEntries == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalEntries / query.pageSize());
    }

    public boolean hasPreviousPage() {
        return query.pageIndex() > 0;
    }

    public boolean hasNextPage() {
        return query.pageIndex() + 1 < totalPages();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
