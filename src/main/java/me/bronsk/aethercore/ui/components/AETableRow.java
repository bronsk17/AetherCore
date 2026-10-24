package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One table row: a left-to-right list of pre-built cells - optionally clickable with a hover highlight, and optionally tinted to show a staged (not yet saved) change. */
public final class AETableRow extends AEUIComponent {

    /** Structural template choice, fixed at construction - a staged addition/removal not yet saved via the page's own Save. */
    public enum Variant { NORMAL, ADDED, REMOVED }

    private static final String TEMPLATE = "Pages/AetherCore/Components/Table/AETableRow.ui";
    private static final String TEMPLATE_CLICKABLE = "Pages/AetherCore/Components/Table/AETableRowClickable.ui";
    private static final String TEMPLATE_CLICKABLE_ADDED = "Pages/AetherCore/Components/Table/AETableRowClickableAdded.ui";
    private static final String TEMPLATE_CLICKABLE_REMOVED = "Pages/AetherCore/Components/Table/AETableRowClickableRemoved.ui";

    private final List<AEUIComponent> cells;
    private final Runnable onClick;
    private final Variant variant;

    public AETableRow(List<AEUIComponent> cells) {
        this(cells, null, Variant.NORMAL);
    }

    /** A clickable data row (never used for the header) - hovering it highlights the row natively. */
    public AETableRow(List<AEUIComponent> cells, Runnable onClick) {
        this(cells, onClick, Variant.NORMAL);
    }

    /** A clickable data row tinted to show a staged (not yet saved) addition or removal. */
    public AETableRow(List<AEUIComponent> cells, Runnable onClick, Variant variant) {
        this.cells = cells;
        this.onClick = onClick;
        this.variant = variant;
    }

    @Override
    protected String getTemplate() {
        if (onClick == null) {
            return TEMPLATE;
        }
        return switch (variant) {
            case ADDED -> TEMPLATE_CLICKABLE_ADDED;
            case REMOVED -> TEMPLATE_CLICKABLE_REMOVED;
            case NORMAL -> TEMPLATE_CLICKABLE;
        };
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("", cells);
        if (onClick != null) {
            content.put("OnActivate", onClick);
        }
        return content;
    }
}
