package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * One colored text run within a {@link AEColoredText} row - one shared template, {@code
 * Style.TextColor} set dynamically through the framework's generic plain-value path (confirmed
 * working live: a dotted path into a compound {@code Style} property is a valid {@code Set} command
 * selector, unlike setting a whole {@code Message} value directly on {@code Text} - that one really
 * doesn't work).
 */
final class AEColorRun extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEColorRun.ui";

    private final String text;
    private final String color;

    AEColorRun(String text, String color) {
        this.text = text;
        this.color = color;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Text", text);
        if (color != null) {
            content.put("Style.TextColor", color);
        }
        return content;
    }
}
