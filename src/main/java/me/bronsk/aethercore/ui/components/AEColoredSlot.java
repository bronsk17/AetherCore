package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Binds directly onto whatever scope it's given, optionally tinting that scope's own {@code
 * Background}, and appends {@code content} as a fresh child inside it - lets a caller override one
 * wrapped component's container background (e.g. a modified/added/removed tint) without that
 * component needing to know about it itself. Uses a {@link LinkedHashMap} so {@code Background} is
 * always applied before the child is appended - {@code Map.of()}/{@link java.util.HashMap} give no
 * ordering guarantee, and setting a property before vs. after a scope has content might not be
 * interchangeable on the native side.
 */
public final class AEColoredSlot extends AEUIComponent {

    private final String background;
    private final AEUIComponent content;

    public AEColoredSlot(String background, AEUIComponent content) {
        this.background = background;
        this.content = content;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> result = new LinkedHashMap<>();
        if (background != null) {
            result.put("Background", background);
        }
        result.put("", content);
        return result;
    }
}
