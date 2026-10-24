package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/** One line of a {@link AEColoredText} - a horizontal row of colored runs, own template so several can be stacked top to bottom. */
final class AEColoredTextLine extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEColoredTextLine.ui";

    private final List<AEUIComponent> runs;

    AEColoredTextLine(List<AEUIComponent> runs) {
        this.runs = runs;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", runs);
    }
}
