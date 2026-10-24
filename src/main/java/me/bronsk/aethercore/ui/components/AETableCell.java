package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/** A single table cell: one label with equal FlexWeight so a row's columns line up under the header. A header cell can optionally be clickable (see {@link AETable}'s sortable constructors). */
public final class AETableCell extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Table/AETableCell.ui";
    private static final String TEMPLATE_HEADER = "Pages/AetherCore/Components/Table/AETableCellHeader.ui";
    private static final String TEMPLATE_HEADER_SORTABLE = "Pages/AetherCore/Components/Table/AETableCellHeaderSortable.ui";

    private final String text;
    private final boolean header;
    private final Runnable onClick;

    public AETableCell(String text) {
        this(text, false, null);
    }

    public AETableCell(String text, boolean header) {
        this(text, header, null);
    }

    /** A clickable sortable header cell - see {@link AETable}'s sortable constructors. */
    public AETableCell(String text, Runnable onClick) {
        this(text, true, onClick);
    }

    private AETableCell(String text, boolean header, Runnable onClick) {
        this.text = text;
        this.header = header;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        if (onClick != null) {
            return TEMPLATE_HEADER_SORTABLE;
        }
        return header ? TEMPLATE_HEADER : TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Text", new AELabel(text));
        if (onClick != null) {
            content.put("OnActivate", onClick);
        }
        return content;
    }
}
