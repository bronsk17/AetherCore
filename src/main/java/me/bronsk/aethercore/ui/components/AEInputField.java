package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** One settings-form row: a label and an arbitrary input control, no preview - see {@link AEInputPreview} for the variant with a live preview below the input. */
public final class AEInputField extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AEInputField.ui";

    private final String label;
    private final AEUIComponent input;

    public AEInputField(String label, AEUIComponent input) {
        this.label = label;
        this.input = input;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Label", new AELabel(label), "Input", input);
    }
}
