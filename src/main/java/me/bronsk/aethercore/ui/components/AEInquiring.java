package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The generic "inquiring" popup: lets the admin pick one {@code T} out of an existing
 * paginated/searchable set, for any field that must choose from a fixed set of values rather than
 * accept free text (e.g. a lang message code). Clicking a row selects it (highlighted, the popup
 * stays open) - Confirm hands that row to {@code onConfirm} and closes, Cancel closes without
 * calling it. Show it via {@link AEUIComponent#showPopup} from the readonly field that opens it.
 *
 * <p>Not used by Permissions' own search/multi-select popups ({@code AEUIAddGroupsPopup} and
 * friends) - those predate this pattern and stay exactly as they are.
 */
public final class AEInquiring<T> extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Popups/AEInquiring.ui";

    private final String title;
    private final List<AEUIColDef<T>> columns;
    private final String emptyText;
    private final Function<AEPageQuery, AEPageResult<T>> fetcher;
    private final Function<T, String> keySelector;
    private final Consumer<T> onConfirm;
    private T selected;

    public AEInquiring(String title, List<AEUIColDef<T>> columns, String emptyText,
                        Function<AEPageQuery, AEPageResult<T>> fetcher, Function<T, String> keySelector, Consumer<T> onConfirm) {
        this.title = title;
        this.columns = columns;
        this.emptyText = emptyText;
        this.fetcher = fetcher;
        this.keySelector = keySelector;
        this.onConfirm = onConfirm;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(title),
                "Content.Table", new AETable<>("", columns, emptyText, fetcher, this::onRowClick, this::rowVariant, true),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel)
        );
    }

    private void onRowClick(T row) {
        selected = row;
        refresh("Content.Table");
    }

    private AETableRow.Variant rowVariant(T row) {
        boolean isSelected = selected != null && keySelector.apply(row).equals(keySelector.apply(selected));
        return isSelected ? AETableRow.Variant.ADDED : AETableRow.Variant.NORMAL;
    }

    private void confirm() {
        closePopup();
        if (selected != null) {
            onConfirm.accept(selected);
        }
    }

    private void cancel() {
        closePopup();
    }
}
