package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * A single tab button, styled differently when it's the active tab, and laid out differently for a
 * vertical sidebar vs. a horizontal in-view tab bar - both are structural choices, fixed at
 * construction (a real state change needs a new instance, same as any other component here).
 */
public final class AETabButton extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Buttons/AETabButton.ui";
    private static final String TEMPLATE_ACTIVE = "Pages/AetherCore/Components/Buttons/AETabButtonActive.ui";
    private static final String TEMPLATE_HORIZONTAL = "Pages/AetherCore/Components/Buttons/AETabButtonHorizontal.ui";
    private static final String TEMPLATE_HORIZONTAL_ACTIVE = "Pages/AetherCore/Components/Buttons/AETabButtonHorizontalActive.ui";

    private final boolean active;
    private final boolean vertical;
    private final String text;
    private final Runnable onClick;

    public AETabButton(String text, boolean active, Runnable onClick) {
        this(text, active, onClick, true);
    }

    public AETabButton(String text, boolean active, Runnable onClick, boolean vertical) {
        this.active = active;
        this.vertical = vertical;
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        if (vertical) {
            return active ? TEMPLATE_ACTIVE : TEMPLATE;
        }
        return active ? TEMPLATE_HORIZONTAL_ACTIVE : TEMPLATE_HORIZONTAL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Text", text, "OnActivate", onClick);
    }
}
