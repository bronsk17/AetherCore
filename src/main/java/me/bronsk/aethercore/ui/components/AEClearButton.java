package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * A small square "x" button - clears/resets whatever it sits beside. Always rendered, same as {@link
 * AEIconButton} - {@code disabled} greys it out and stops it from activating via the native {@code
 * Button.Disabled} property, instead of the button appearing/disappearing (which would shift the
 * layout around it).
 */
public final class AEClearButton extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Buttons/AEClearButton.ui";

    private final Runnable onClick;
    private final BooleanSupplier disabled;

    public AEClearButton(Runnable onClick) {
        this(onClick, () -> false);
    }

    public AEClearButton(Runnable onClick, BooleanSupplier disabled) {
        this.onClick = onClick;
        this.disabled = disabled;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("OnActivate", onClick, "Disabled", disabled.getAsBoolean());
    }
}
