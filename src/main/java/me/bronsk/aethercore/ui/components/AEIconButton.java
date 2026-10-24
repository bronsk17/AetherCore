package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.BooleanSupplier;

/** A small icon-only button - same shape as {@link AEActionButton} but with a native icon instead of a text label. */
public final class AEIconButton extends AEUIComponent {

    /** Structural template choice, fixed at construction - picks which native icon asset to show. */
    public enum Icon { TRASH, UNDO, REFRESH }

    private static final String TEMPLATE_TRASH = "Pages/AetherCore/Components/Buttons/AEIconButtonTrash.ui";
    private static final String TEMPLATE_UNDO = "Pages/AetherCore/Components/Buttons/AEIconButtonUndo.ui";
    private static final String TEMPLATE_REFRESH = "Pages/AetherCore/Components/Buttons/AEIconButtonRefresh.ui";

    private final Icon icon;
    private final Runnable onClick;
    private final BooleanSupplier disabled;

    public AEIconButton(Icon icon, Runnable onClick) {
        this(icon, onClick, () -> false);
    }

    /** @param disabled Re-evaluated every time this button re-renders (e.g. whenever a table's selection changes) - greys the button out and stops it from activating while true, via the native {@code Button.Disabled} property. */
    public AEIconButton(Icon icon, Runnable onClick, BooleanSupplier disabled) {
        this.icon = icon;
        this.onClick = onClick;
        this.disabled = disabled;
    }

    @Override
    protected String getTemplate() {
        return switch (icon) {
            case TRASH -> TEMPLATE_TRASH;
            case UNDO -> TEMPLATE_UNDO;
            case REFRESH -> TEMPLATE_REFRESH;
        };
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("OnActivate", onClick, "Disabled", disabled.getAsBoolean());
    }
}
