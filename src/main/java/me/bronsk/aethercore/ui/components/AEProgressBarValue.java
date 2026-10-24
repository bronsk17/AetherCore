package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * A ProgressBar's Value (0-1) bound directly to whatever scope it's given - no template of its
 * own, same "bind directly onto an already-declared element" pattern as {@link AELabel}. An
 * optional {@code barColor} overrides the markup's own {@code Bar} fill color at runtime.
 */
public final class AEProgressBarValue extends AEUIComponent {

    private final float value;
    private final String barColor;

    public AEProgressBarValue(float value) {
        this(value, null);
    }

    public AEProgressBarValue(float value, String barColor) {
        this.value = value;
        this.barColor = barColor;
    }

    @Override
    protected Map<String, Object> getContent() {
        if (barColor == null) {
            return Map.of("Value", value);
        }
        Map<String, Object> content = new HashMap<>();
        content.put("Value", value);
        content.put("Bar", barColor);
        return content;
    }
}
