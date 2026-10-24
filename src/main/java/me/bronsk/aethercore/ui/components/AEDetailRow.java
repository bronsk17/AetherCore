package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A single label/value row in a borderless key-value table. */
public final class AEDetailRow extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEDetailRow.ui";

    private final String label;
    private final String value;

    public AEDetailRow(String label, String value) {
        this.label = label;
        this.value = value;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Label", new AELabel(label), "Value", new AELabel(value));
    }
}
