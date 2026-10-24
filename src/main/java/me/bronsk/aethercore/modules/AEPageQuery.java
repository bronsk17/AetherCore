package me.bronsk.aethercore.modules;

/**
 * Generic page request - pageIndex/pageSize/search - shared by every paginated listing in this
 * plugin (storage, module, and UI layers alike) instead of each domain (players, groups,
 * permissions, lang messages, ...) redefining an identical record.
 *
 * @param pageIndex Zero-based page index.
 * @param pageSize Number of items requested for the page.
 * @param search Free-text search filter, empty when none.
 */
public record AEPageQuery(int pageIndex, int pageSize, String search) {

    public AEPageQuery {
        if (pageIndex < 0) {
            throw new IllegalArgumentException("pageIndex must be greater than or equal to zero");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize must be greater than zero");
        }
        search = search == null ? "" : search.strip();
    }

    public AEPageQuery(int pageIndex, int pageSize) {
        this(pageIndex, pageSize, "");
    }

    public long offset() {
        return (long) pageIndex * pageSize;
    }

    public boolean hasSearch() {
        return !search.isEmpty();
    }
}
