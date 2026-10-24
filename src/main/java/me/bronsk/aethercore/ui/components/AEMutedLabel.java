package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A small dim, non-interactive label - e.g. a numeric id shown next to a page title. */
public final class AEMutedLabel extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEMutedLabel.ui";

    private final String text;

    public AEMutedLabel(String text) {
        this.text = text;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Text", text);
    }
}
