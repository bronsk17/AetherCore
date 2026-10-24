package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A single label, centered in whatever container it's given. */
public final class AECenteredLabel extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AECenteredLabel.ui";

    private final String text;

    public AECenteredLabel(String text) {
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
