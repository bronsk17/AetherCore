package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/** One column (generation) of a {@link AEGroupChainLayout.Chain}: its nodes stacked top-to-bottom, with a fixed right margin separating it from the next column. */
final class AEGroupChainColumn extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEGroupChainColumn.ui";

    private final List<AEUIComponent> nodes;

    AEGroupChainColumn(List<AEUIComponent> nodes) {
        this.nodes = nodes;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", nodes);
    }
}
