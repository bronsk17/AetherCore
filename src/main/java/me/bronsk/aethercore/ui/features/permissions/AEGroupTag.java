package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AELabel;

import java.util.Map;

/** A small clickable "[group name]" chip - the group-picking counterpart to {@code AEPlayerTag}. */
public final class AEGroupTag extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEGroupTagNeutral.ui";
    private static final String TEMPLATE_STAGED = "Pages/AetherCore/Features/Permissions/AEGroupTagSuccess.ui";

    private final String name;
    private final boolean staged;
    private final Runnable onClick;

    public AEGroupTag(String name, boolean staged, Runnable onClick) {
        this.name = name;
        this.staged = staged;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        return staged ? TEMPLATE_STAGED : TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("OnActivate", onClick, "Name", new AELabel(name));
    }
}
