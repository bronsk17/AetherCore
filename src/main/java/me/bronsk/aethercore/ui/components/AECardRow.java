package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** A titled row for {@link AECardList}: bold title + dim description on the left, an arbitrary trailing component on the right. */
public final class AECardRow extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Cards/AECardRow.ui";

    private final String title;
    private final String description;
    private final AEUIComponent trailing;

    public AECardRow(String title, String description, AEUIComponent trailing) {
        this.title = title;
        this.description = description;
        this.trailing = trailing;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Text.Title", new AELabel(title),
                "Text.Description", new AELabel(description == null || description.isBlank() ? "" : description),
                "Trailing", trailing
        );
    }
}
