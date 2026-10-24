package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A single small square button (prev/next arrows, numbered page buttons), optionally highlighted as active. */
public final class AESmallButton extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Buttons/AESmallButton.ui";
    private static final String TEMPLATE_ACTIVE = "Pages/AetherCore/Components/Buttons/AESmallButtonActive.ui";

    private final boolean active;
    private final String label;
    private final Runnable onClick;

    public AESmallButton(String label, Runnable onClick) {
        this(label, false, onClick);
    }

    public AESmallButton(String label, boolean active, Runnable onClick) {
        this.active = active;
        this.label = label;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        return active ? TEMPLATE_ACTIVE : TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("OnActivate", onClick, "Label", new AELabel(label));
    }
}
