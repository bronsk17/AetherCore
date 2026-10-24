package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A plain native text field (not $Common.@HeaderSearch) used as a search box. Its owner must never
 * let this component's own key be part of a {@link #refresh} - the underlying text field must stay
 * the same element for the whole session, or the player loses focus/cursor position mid-keystroke.
 */
public final class AESearch extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AESearch.ui";

    private final String placeholder;
    private final String initialValue;
    private final Consumer<String> onChange;

    public AESearch(String placeholder, String initialValue, Consumer<String> onChange) {
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
