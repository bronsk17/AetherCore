package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/** A titled container: a static section title above arbitrary child content. */
public final class AESection extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AESection.ui";

    private final String title;
    private final List<AEUIComponent> content;

    public AESection(String title, List<AEUIComponent> content) {
        this.title = title;
        this.content = content;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Title", new AELabel(title), "Content", content);
    }
}
