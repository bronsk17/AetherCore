package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A searchable, paginated vertical list, generic over its item type - {@link AETable} is for
 * multi-column tables, this is for free-form rows (e.g. a permission node + its tri-state
 * selector). {@code rowRenderer} builds the entire row's shape and receives this list's own
 * {@code refreshRows} callback so a row's own interactive control can trigger a re-fetch+redraw
 * without needing cross-instance access to a sibling component - the same reasoning already
 * applied to {@code AEUIModerationEntryPopup} receiving its callbacks from its parent.
 */
public final class AECardList<T> extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Cards/AECardList.ui";
    private static final int DEFAULT_PAGE_SIZE = 10;

    private final String title;
    private final String emptyText;
    private final Function<AEPageQuery, AEPageResult<T>> fetcher;
    private final BiFunction<T, Runnable, AEUIComponent> rowRenderer;
    private AEPageQuery query;

    public AECardList(String title, String emptyText, Function<AEPageQuery, AEPageResult<T>> fetcher,
                       BiFunction<T, Runnable, AEUIComponent> rowRenderer) {
        this.title = title;
        this.emptyText = emptyText;
        this.fetcher = fetcher;
        this.rowRenderer = rowRenderer;
        this.query = new AEPageQuery(0, DEFAULT_PAGE_SIZE, "");
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        AEPageResult<T> result = fetcher.apply(query);
        return Map.of(
                "TitleBar.Title", new AELabel(title),
                "TitleBar.Search", List.of(new AESearch(lang("ui.common.search_placeholder"), query.search(), this::onSearchChanged)),
                "Rows", getRowComponents(result.items()),
                "Pagination", new AEPagination(query.pageIndex(), query.pageSize(), result.totalPages(), result.totalItems(),
                        this::goToPage, this::changePageSize)
        );
    }

    private List<AEUIComponent> getRowComponents(List<T> items) {
        if (items.isEmpty()) {
            return List.of(new AEEmptyMessage(emptyText));
        }
        return items.stream().map(item -> rowRenderer.apply(item, this::refreshRows)).toList();
    }

    private void refreshRows() {
        refresh("Rows", "Pagination");
    }

    private void onSearchChanged(String value) {
        query = new AEPageQuery(0, query.pageSize(), value);
        refresh("Rows", "Pagination");
    }

    private void goToPage(int pageIndex) {
        if (pageIndex < 0) {
            return;
        }
        query = new AEPageQuery(pageIndex, query.pageSize(), query.search());
        refresh("Rows", "Pagination");
    }

    private void changePageSize(int newSize) {
        query = new AEPageQuery(0, newSize, query.search());
        refresh("Rows", "Pagination");
    }
}
