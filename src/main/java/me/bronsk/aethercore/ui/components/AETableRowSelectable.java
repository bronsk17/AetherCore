package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Wraps one {@link AETableRow} with a leading selection checkbox, as a sibling rather than nested
 * inside the row's own clickable Button - Hytale's native UI has no demonstrated support for a
 * clickable nested inside another clickable, so row-click and checkbox-click must be independent
 * sibling elements (matching the native precedent in {@code PluginListButton.ui}, which pairs a row
 * button and a checkbox the same way). {@code checked == null} renders a blank spacer instead of an
 * actual checkbox - used for the header row, so columns still line up under their labels.
 */
final class AETableRowSelectable extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Table/AETableRowSelectable.ui";

    private final Boolean checked;
    private final Consumer<Boolean> onToggle;
    private final AETableRow row;

    AETableRowSelectable(Boolean checked, Consumer<Boolean> onToggle, AETableRow row) {
        this.checked = checked;
        this.onToggle = onToggle;
        this.row = row;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Select", checked == null ? List.of() : List.of(new AECheckbox(checked, onToggle)),
                "Content", row
        );
    }
}
