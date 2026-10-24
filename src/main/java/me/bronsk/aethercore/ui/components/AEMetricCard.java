package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A big value over a small muted label - used for the Overview tab's top metric row (players online, RAM/CPU usage) and other feature-level summary cards (e.g. Player Overview's World/Position). */
public final class AEMetricCard extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Cards/AEMetricCard.ui";

    private final String value;
    private final String valueColor;
    private final String label;

    public AEMetricCard(String value, String label) {
        this(value, null, label);
    }

    /** @param valueColor Overrides the value's default text color (e.g. a RAM/CPU threshold color) - null keeps the default. */
    public AEMetricCard(String value, String valueColor, String label) {
        this.value = value;
        this.valueColor = valueColor;
        this.label = label;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Value", new AELabel(value, valueColor), "Label", new AEMutedLabel(label));
    }
}
