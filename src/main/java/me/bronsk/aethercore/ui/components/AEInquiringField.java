package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * A readonly field showing the current value of an "inquiring" selection - clicking it runs {@code
 * onClick} (normally {@code showPopup(new AEInquiring<>(...))}). Pair this with {@link AEInquiring}
 * for any field that must choose from a fixed set of values instead of accepting free text.
 *
 * <p>When {@code isNullable} and a value is set, a trailing "x" ({@link AEClearButton}) appears -
 * always rendered, greyed out via {@code Button.Disabled} when there's nothing to clear, so the
 * layout never shifts between states. Clicking it while enabled runs {@code onClear} instead of
 * opening the picker, resetting the value to null.
 *
 * <p>{@code fieldBackground} (e.g. a modified/added/removed diff tint) colors ONLY the value box
 * itself, not the whole row including the "x" - applied via {@link AEColoredSlot} wrapping just the
 * "Field" child, so the clear button keeps its own independent look regardless of diff state. Must be
 * a plain {@code #RRGGBB} hex (no alpha-paren suffix) - see {@code project_native_ui_migration} memory
 * for why an alpha color silently breaks when set dynamically at runtime.
 */
public final class AEInquiringField extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AEInquiringField.ui";

    private final String value;
    private final Runnable onClick;
    private final boolean isNullable;
    private final Runnable onClear;
    private final String fieldBackground;

    public AEInquiringField(String value, Runnable onClick) {
        this(value, onClick, false, null, null);
    }

    public AEInquiringField(String value, Runnable onClick, boolean isNullable, Runnable onClear) {
        this(value, onClick, isNullable, onClear, null);
    }

    public AEInquiringField(String value, Runnable onClick, boolean isNullable, Runnable onClear, String fieldBackground) {
        this.value = value;
        this.onClick = onClick;
        this.isNullable = isNullable;
        this.onClear = onClear;
        this.fieldBackground = fieldBackground;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        String displayText = value == null || value.isBlank() ? "(none)" : value;
        boolean canClear = isNullable && value != null && !value.isBlank();
        BooleanSupplier disabled = () -> !canClear;
        return Map.of(
                "Field", new AEColoredSlot(fieldBackground, new AEInquiringFieldButton(displayText, onClick)),
                "Clear", new AEClearButton(canClear ? onClear : () -> { }, disabled)
        );
    }
}
