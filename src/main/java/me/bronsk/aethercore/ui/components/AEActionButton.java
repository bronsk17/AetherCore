package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A wider labeled button for moderation actions (e.g. Ban, Kick, Freeze). */
public final class AEActionButton extends AEUIComponent {

    /** Structural template choice, fixed at construction - a real color change needs a new instance. */
    public enum Variant { NORMAL, DANGER, PRIMARY }

    private static final String TEMPLATE = "Pages/AetherCore/Components/Buttons/AEActionButton.ui";
    private static final String TEMPLATE_DANGER = "Pages/AetherCore/Components/Buttons/AEActionButtonDanger.ui";
    private static final String TEMPLATE_PRIMARY = "Pages/AetherCore/Components/Buttons/AEActionButtonPrimary.ui";

    private final String label;
    private final Variant variant;
    private final Runnable onClick;

    public AEActionButton(String label, Runnable onClick) {
        this(label, Variant.NORMAL, onClick);
    }

    /** @param danger Picks the red/destructive look (e.g. "Delete", "Revoke") instead of the default one. */
    public AEActionButton(String label, boolean danger, Runnable onClick) {
        this(label, danger ? Variant.DANGER : Variant.NORMAL, onClick);
    }

    public AEActionButton(String label, Variant variant, Runnable onClick) {
        this.label = label;
        this.variant = variant;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        return switch (variant) {
            case DANGER -> TEMPLATE_DANGER;
            case PRIMARY -> TEMPLATE_PRIMARY;
            case NORMAL -> TEMPLATE;
        };
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("OnActivate", onClick, "Label", new AELabel(label));
    }
}
