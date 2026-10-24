package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.Consumer;

/** A native checkbox toggle - wraps the native {@code $Common.@CheckBox} macro. */
public final class AECheckbox extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AECheckbox.ui";

    private final boolean checked;
    private final Consumer<Boolean> onChange;

    public AECheckbox(boolean checked, Consumer<Boolean> onChange) {
        this.checked = checked;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Consumer<String> onValueChanged = value -> onChange.accept(Boolean.parseBoolean(value));
        return Map.of("Value", checked, "OnValueChanged", onValueChanged);
    }
}
