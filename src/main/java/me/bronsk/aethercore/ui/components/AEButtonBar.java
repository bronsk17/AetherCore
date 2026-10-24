package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/** A row of buttons pushed to the right edge of its container via a leading flexible spacer. */
public final class AEButtonBar extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Buttons/AEButtonBar.ui";

    private final List<AEUIComponent> buttons;

    public AEButtonBar(List<AEUIComponent> buttons) {
        this.buttons = buttons;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Buttons", buttons);
    }
}
