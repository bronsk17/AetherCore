package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A searchable, paginated table, generic over its row type and driven by a {@code fetcher} - the
 * caller owns its own concrete storage query and adapts it to {@link AEPageQuery}/{@link
 * AEPageResult} (often trivially, since most storage layers already speak these types directly).
 * Optionally clickable: supply {@code onRowClick} and each data row becomes a real button with a
 * native hover highlight (see {@code TableRowClickable.ui}).
 *
 * <p>A non-null {@code keySelector} (full constructor) turns on a per-row selection checkbox
 * (rendered as a sibling of the row, not nested inside its clickable Button - Hytale's native UI
 * has no demonstrated support for a clickable nested inside another clickable) and shows {@code
 * customButtons} to the left of the search box. This table does NOT own the checked set itself -
 * {@code selectedKeys} is supplied fresh by the caller on every render and {@code onToggleSelect}
 * reports every check/uncheck back to it. This is deliberate, not just a style choice: {@code
 * getContent()} callers throughout this codebase build a brand new {@code AETable} instance on
 * every refresh (e.g. after a batch action), so any selection state kept as a field on `this`
 * would be silently discarded the moment that happens - the caller is the only thing that survives
 * across those refreshes, so it has to be the one holding the {@code Set}. Selection changes ONLY
 * on an explicit checkbox click - a batch action (delete, undo, ...) must never itself add to or
 * clear the caller's set, so a row an admin already checked stays checked (and a red/removed row
 * stays checked too) until they uncheck it themselves, no matter how many batch actions run in
 * between.
 *
 * <p>{@code sortable = true} turns header cells into buttons that sort the rows already on the
 * current page - client-side, not by changing {@code fetcher}'s query, per {@link AEUIColDef}'s own
 * {@code comparator}. Clicking a column cycles it ascending -> descending -> unsorted; clicking a
 * second column while the first is still active adds it as a secondary key (ties on the first
 * column break on the second), shown by a small rank number next to its arrow once 2+ columns are
 * active. {@code defaultSort}, when given, orders the rows whenever no column is actively clicked.
 */
public final class AETable<T> extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Table/AETable.ui";
    private static final int DEFAULT_PAGE_SIZE = 20;

    private enum SortDirection { ASC, DESC }

    private final String title;
    private final List<AEUIColDef<T>> columns;
    private final String emptyText;
    private final Function<AEPageQuery, AEPageResult<T>> fetcher;
    private final Consumer<T> onRowClick;
    private final Function<T, AETableRow.Variant> rowVariant;
    private final Function<T, String> keySelector;
    private final Set<String> selectedKeys;
    private final BiConsumer<String, Boolean> onToggleSelect;
    private final List<AEUIComponent> customButtons;
    private final boolean sortable;
    private final Comparator<T> defaultSort;
    /** Column index -> direction, in click order (insertion order = sort priority, primary key first). */
    private final LinkedHashMap<Integer, SortDirection> activeSorts = new LinkedHashMap<>();
    private AEPageQuery query;

    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText, Function<AEPageQuery, AEPageResult<T>> fetcher) {
        this(title, columns, emptyText, fetcher, null);
    }

    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick) {
        this(title, columns, emptyText, fetcher, onRowClick, row -> AETableRow.Variant.NORMAL);
    }

    /** Same as the other constructors, but each row's tint (staged addition/removal, or normal) comes from {@code rowVariant}. */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, Function<T, AETableRow.Variant> rowVariant) {
        this(title, columns, emptyText, fetcher, onRowClick, rowVariant, null, Set.of(), (key, checked) -> { }, List.of());
    }

    /** Same as the plain-clickable constructor, but with client-side column sorting - see the class doc. */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, boolean sortable) {
        this(title, columns, emptyText, fetcher, onRowClick, sortable, null);
    }

    /** Same as above, plus {@code defaultSort} - the order used while no column is actively clicked. */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, boolean sortable, Comparator<T> defaultSort) {
        this(title, columns, emptyText, fetcher, onRowClick, row -> AETableRow.Variant.NORMAL, null, Set.of(), (key, checked) -> { }, List.of(), sortable, defaultSort);
    }

    /** Same as the rowVariant constructor, but with client-side column sorting - see the class doc. */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, Function<T, AETableRow.Variant> rowVariant, boolean sortable) {
        this(title, columns, emptyText, fetcher, onRowClick, rowVariant, null, Set.of(), (key, checked) -> { }, List.of(), sortable, null);
    }

    /**
     * Full selectable constructor - see the class doc for {@code keySelector}/{@code
     * customButtons}/{@code selectedKeys}. {@code selectedKeys} is the CALLER's own set (read fresh
     * on every render, never copied or retained beyond this render), and {@code onToggleSelect} is
     * how a checkbox click reports back to it - this table never mutates {@code selectedKeys}
     * itself.
     */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, Function<T, AETableRow.Variant> rowVariant,
                   Function<T, String> keySelector, Set<String> selectedKeys, BiConsumer<String, Boolean> onToggleSelect,
                   List<AEUIComponent> customButtons) {
        this(title, columns, emptyText, fetcher, onRowClick, rowVariant, keySelector, selectedKeys, onToggleSelect, customButtons, false, null);
    }

    /** Same as the full (selectable) constructor, but with client-side column sorting - see the class doc. */
    public AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                   Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, Function<T, AETableRow.Variant> rowVariant,
                   Function<T, String> keySelector, Set<String> selectedKeys, BiConsumer<String, Boolean> onToggleSelect,
                   List<AEUIComponent> customButtons, boolean sortable) {
        this(title, columns, emptyText, fetcher, onRowClick, rowVariant, keySelector, selectedKeys, onToggleSelect, customButtons, sortable, null);
    }

    private AETable(String title, List<AEUIColDef<T>> columns, String emptyText,
                     Function<AEPageQuery, AEPageResult<T>> fetcher, Consumer<T> onRowClick, Function<T, AETableRow.Variant> rowVariant,
                     Function<T, String> keySelector, Set<String> selectedKeys, BiConsumer<String, Boolean> onToggleSelect,
                     List<AEUIComponent> customButtons, boolean sortable, Comparator<T> defaultSort) {
        this.title = title;
        this.columns = columns;
        this.emptyText = emptyText;
        this.fetcher = fetcher;
        this.onRowClick = onRowClick;
        this.rowVariant = rowVariant;
        this.keySelector = keySelector;
        this.selectedKeys = selectedKeys;
        this.onToggleSelect = onToggleSelect;
        this.customButtons = customButtons;
        this.sortable = sortable;
        this.defaultSort = defaultSort;
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
                "TitleBar.Actions", customButtons,
                "TitleBar.Search", List.of(new AESearch(lang("ui.common.search_placeholder"), query.search(), this::onSearchChanged)),
                "Header", getHeaderRow(),
                "Rows", getRowComponents(sortedRows(result.items())),
                "Pagination", new AEPagination(query.pageIndex(), query.pageSize(), result.totalPages(), result.totalItems(),
                        this::goToPage, this::changePageSize)
        );
    }

    /** Sorts the rows already on this page - see the class doc; not a query-level sort. */
    private List<T> sortedRows(List<T> rows) {
        Comparator<T> comparator = buildComparator();
        if (comparator == null) {
            return rows;
        }
        List<T> sorted = new ArrayList<>(rows);
        sorted.sort(comparator);
        return sorted;
    }

    private Comparator<T> buildComparator() {
        if (activeSorts.isEmpty()) {
            return defaultSort;
        }
        Comparator<T> comparator = null;
        for (Map.Entry<Integer, SortDirection> entry : activeSorts.entrySet()) {
            Comparator<T> columnComparator = columns.get(entry.getKey()).comparator();
            if (entry.getValue() == SortDirection.DESC) {
                columnComparator = columnComparator.reversed();
            }
            comparator = comparator == null ? columnComparator : comparator.thenComparing(columnComparator);
        }
        return comparator;
    }

    /** Cycles a column ascending -> descending -> unsorted; a fresh click always becomes the lowest-priority active column. */
    private void onHeaderClick(int columnIndex) {
        SortDirection current = activeSorts.get(columnIndex);
        if (current == null) {
            activeSorts.put(columnIndex, SortDirection.ASC);
        } else if (current == SortDirection.ASC) {
            activeSorts.put(columnIndex, SortDirection.DESC);
        } else {
            activeSorts.remove(columnIndex);
        }
        refresh("Header", "Rows");
    }

    private boolean isSelectable() {
        return keySelector != null;
    }

    private AEUIComponent getHeaderRow() {
        AETableRow header = new AETableRow(getHeaderCells());
        return isSelectable() ? new AETableRowSelectable(null, null, header) : header;
    }

    private List<AEUIComponent> getHeaderCells() {
        List<AEUIComponent> cells = new ArrayList<>();
        List<Integer> sortOrder = List.copyOf(activeSorts.keySet());
        for (int i = 0; i < columns.size(); i++) {
            String text = getHeaderText(columns.get(i), i, sortOrder);
            if (sortable) {
                int columnIndex = i;
                cells.add(new AETableCell(text, () -> onHeaderClick(columnIndex)));
            } else {
                cells.add(new AETableCell(text, true));
            }
        }
        return cells;
    }

    /** Appends a direction arrow to an actively-sorted column's header, plus its rank (1-based) once 2+ columns are active - see the class doc. */
    private String getHeaderText(AEUIColDef<T> column, int columnIndex, List<Integer> sortOrder) {
        SortDirection direction = activeSorts.get(columnIndex);
        if (!sortable || direction == null) {
            return column.header();
        }
        // Plain ASCII on purpose - the game font renders unicode arrows (▲/▼) as "?" (tofu), like
        // every other glyph in this UI, e.g. AEPagination's "<"/">" prev/next buttons.
        String arrow = direction == SortDirection.ASC ? " ^" : " v";
        String rank = sortOrder.size() > 1 ? " " + (sortOrder.indexOf(columnIndex) + 1) : "";
        return column.header() + arrow + rank;
    }

    private List<AEUIComponent> getRowComponents(List<T> rows) {
        if (rows.isEmpty()) {
            return List.of(new AEEmptyMessage(emptyText));
        }
        return rows.stream().map(this::newRow).toList();
    }

    private AEUIComponent newRow(T row) {
        AETableRow tableRow = buildTableRow(row);
        if (!isSelectable()) {
            return tableRow;
        }
        String key = keySelector.apply(row);
        boolean checked = selectedKeys.contains(key);
        return new AETableRowSelectable(checked, isChecked -> {
            onToggleSelect.accept(key, isChecked);
            // Re-renders just the action buttons so a Trash/Undo button's disabled state reflects
            // the selection live, without touching the rows or resetting anyone's scroll/page.
            refresh("TitleBar.Actions");
        }, tableRow);
    }

    private AETableRow buildTableRow(T row) {
        List<AEUIComponent> cells = getCells(row);
        return onRowClick == null ? new AETableRow(cells) : new AETableRow(cells, () -> onRowClick.accept(row), rowVariant.apply(row));
    }

    private List<AEUIComponent> getCells(T row) {
        List<AEUIComponent> cells = new ArrayList<>();
        for (AEUIColDef<T> column : columns) {
            cells.add(new AETableCell(column.accessor().apply(row)));
        }
        return cells;
    }

    private void onSearchChanged(String value) {
        query = new AEPageQuery(0, query.pageSize(), value);
        refresh("Header", "Rows", "Pagination");
    }

    private void goToPage(int pageIndex) {
        if (pageIndex < 0) {
            return;
        }
        query = new AEPageQuery(pageIndex, query.pageSize(), query.search());
        refresh("Header", "Rows", "Pagination");
    }

    private void changePageSize(int newSize) {
        query = new AEPageQuery(0, newSize, query.search());
        refresh("Header", "Rows", "Pagination");
    }
}
