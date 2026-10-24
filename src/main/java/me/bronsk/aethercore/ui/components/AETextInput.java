package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.Consumer;

/** A plain native single-line text field - same underlying widget as {@link AESearch}, generic label. */
public final class AETextInput extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AETextInput.ui";

    private final String placeholder;
    private final String initialValue;
    private final Consumer<String> onChange;

    public AETextInput(String placeholder, String initialValue, Consumer<String> onChange) {
        this.placeholder = placeholder;
        this.initialValue = initialValue;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("PlaceholderText", placeholder, "Value", initialValue, "OnValueChanged", onChange);
    }
}
