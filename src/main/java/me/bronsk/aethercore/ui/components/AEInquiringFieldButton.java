package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * The clickable field box inside {@link AEInquiringField} - its own fresh-appended template, exactly
 * mirroring how {@code AETableRowClickable} is appended into {@code AETableRowSelectable}'s "Content"
 * slot: an outer wrapper Group (fixed small slot + Top-mode FlexWeight slot) hosting a separately
 * templated Button, rather than one Button nested two levels deep inside a single template.
 */
final class AEInquiringFieldButton extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AEInquiringFieldButton.ui";

    private final String text;
    private final Runnable onClick;

    AEInquiringFieldButton(String text, Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Text", new AELabel(text), "OnActivate", onClick);
    }
}
