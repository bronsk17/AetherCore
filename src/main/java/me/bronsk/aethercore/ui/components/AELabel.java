package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * A Text value bound directly to whatever scope it's given - no template of its own. Use it to
 * make a CHILD element (a static Label declared inside a parent's own .ui markup) independently
 * addressable. An optional {@code color} overrides the markup's own {@code Style.TextColor} at
 * runtime - a nested-but-flat compound property, confirmed settable this way (unlike a whole
 * multi-state {@code Style} block with {@code Default}/{@code Hovered} sub-states, which isn't).
 */
public final class AELabel extends AEUIComponent {

    private final String text;
    private final String color;

    public AELabel(String text) {
        this(text, null);
    }

    public AELabel(String text, String color) {
        this.text = text;
        this.color = color;
    }

    @Override
    protected Map<String, Object> getContent() {
        if (color == null) {
            return Map.of("Text", text);
        }
        Map<String, Object> content = new HashMap<>();
        content.put("Text", text);
        content.put("Style.TextColor", color);
        return content;
    }
}
