package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/** A generic panel background (native @SimpleContainer) around whatever content is drawn into it. */
public final class AECard extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Cards/AECard.ui";

    private final String layoutMode;
    private final List<AEUIComponent> content;

    public AECard(String layoutMode, List<AEUIComponent> content) {
        this.layoutMode = layoutMode;
        this.content = content;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("LayoutMode", layoutMode, "", content);
    }
}
