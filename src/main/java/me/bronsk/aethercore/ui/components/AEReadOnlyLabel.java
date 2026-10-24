package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * A single Label appended fresh into an empty container slot. Unlike {@link AELabel}, which binds
 * directly onto an existing static Label already declared in the parent's own markup, this has its
 * own template - use it for a slot that must also be able to host a different component type
 * depending on mode (e.g. a code row that's a text input when creating, plain text when editing).
 */
public final class AEReadOnlyLabel extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEReadOnlyLabel.ui";

    private final String text;

    public AEReadOnlyLabel(String text) {
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
