package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A centered placeholder label shown in place of an empty list/grid. */
public final class AEEmptyMessage extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEEmptyMessage.ui";

    private final String text;

    public AEEmptyMessage(String text) {
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
