package me.bronsk.aethercore.ui.components;

import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

/**
 * Prev/next arrows, numbered page buttons, and a page-size dropdown (with a "Max" option that
 * resolves to the current total). Stateless - the owner reconstructs it with fresh numbers and
 * calls {@link AEUIComponent#refresh} on the key it's plugged into whenever the page/count changes.
 */
public final class AEPagination extends AEUIComponent {

    private static final List<Integer> DEFAULT_PAGE_SIZES = List.of(20, 50);
    private static final String MAX_PAGE_SIZE_VALUE = "max";
    private static final int VISIBLE_PAGE_BUTTONS = 5;

    private final int pageIndex;
    private final int pageSize;
    private final int totalPages;
    private final long totalItems;
    private final List<Integer> pageSizes;
    private final IntConsumer onPageChange;
    private final IntConsumer onPageSizeChange;

    public AEPagination(int pageIndex, int pageSize, int totalPages, long totalItems,
                         IntConsumer onPageChange, IntConsumer onPageSizeChange) {
        this(pageIndex, pageSize, totalPages, totalItems, DEFAULT_PAGE_SIZES, onPageChange, onPageSizeChange);
    }

    public AEPagination(int pageIndex, int pageSize, int totalPages, long totalItems, List<Integer> pageSizes,
                         IntConsumer onPageChange, IntConsumer onPageSizeChange) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSizes = pageSizes;
        this.onPageChange = onPageChange;
        this.onPageSizeChange = onPageSizeChange;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<AEUIComponent> items = new ArrayList<>();
        items.add(new AESmallButton("<", () -> onPageChange.accept(pageIndex - 1)));

        int firstVisible = getFirstVisiblePage();
        for (int i = 0; i < VISIBLE_PAGE_BUTTONS && firstVisible + i < totalPages; i++) {
            int target = firstVisible + i;
            items.add(new AESmallButton(Integer.toString(target + 1), target == pageIndex, () -> onPageChange.accept(target)));
        }

        items.add(new AESmallButton(">", () -> {
            if (pageIndex + 1 < totalPages) {
                onPageChange.accept(pageIndex + 1);
            }
        }));

        items.add(getPageSizeDropdown());
        return Map.of("", items);
    }

    private AEUIComponent getPageSizeDropdown() {
        List<DropdownEntryInfo> entries = pageSizes.stream()
                .map(size -> new DropdownEntryInfo(LocalizableString.fromString(Integer.toString(size)), Integer.toString(size)))
                .collect(Collectors.toCollection(ArrayList::new));
        entries.add(new DropdownEntryInfo(LocalizableString.fromString(lang("ui.common.max")), MAX_PAGE_SIZE_VALUE));

        boolean isMax = !pageSizes.contains(pageSize);
        String value = isMax ? MAX_PAGE_SIZE_VALUE : Integer.toString(pageSize);

        return new AEPageSizeDropdown(entries, value, newValue -> {
            int newSize = MAX_PAGE_SIZE_VALUE.equals(newValue)
                    ? (int) Math.max(1, totalItems)
                    : Integer.parseInt(newValue);
            onPageSizeChange.accept(newSize);
        });
    }

    private int getFirstVisiblePage() {
        int centered = Math.max(0, pageIndex - VISIBLE_PAGE_BUTTONS / 2);
        return Math.max(0, Math.min(centered, Math.max(0, totalPages - VISIBLE_PAGE_BUTTONS)));
    }
}
