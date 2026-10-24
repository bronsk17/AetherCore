package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.Consumer;

/** A multi-line native text field - same underlying widget family as {@link AETextInput}, taller. */
public final class AEMultilineTextInput extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AEMultilineTextInput.ui";

    private final String placeholder;
    private final String initialValue;
    private final Consumer<String> onChange;

    public AEMultilineTextInput(String placeholder, String initialValue, Consumer<String> onChange) {
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
