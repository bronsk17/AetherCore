package me.bronsk.aethercore.modules;

import java.util.List;
import java.util.Objects;

/**
 * Generic page result matching a {@link AEPageQuery} - shared across storage, module, and UI layers.
 *
 * @param query Query used to produce this page.
 * @param items Items contained in the requested page.
 * @param totalItems Total number of items matching the query's search filter.
 */
public record AEPageResult<T>(AEPageQuery query, List<T> items, long totalItems) {

    public AEPageResult {
        Objects.requireNonNull(query, "query");
        items = List.copyOf(Objects.requireNonNull(items, "items"));
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be greater than or equal to zero");
        }
    }

    public int totalPages() {
        if (totalItems == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / query.pageSize());
    }

    public boolean hasPreviousPage() {
        return query.pageIndex() > 0;
    }

    public boolean hasNextPage() {
        return query.pageIndex() + 1 < totalPages();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
