package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One box in the Groups Perms "Grid" view: a group's name, member count and priority, clickable to
 * open its details - {@link AEGroupChainLayout} decides where it sits, this only renders it. Two
 * structural template variants (normal / compact) let a long chain shrink to fit the panel width
 * without ever setting a size dynamically.
 */
final class AEGroupGraphNode extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEGroupGraphNode.ui";
    private static final String TEMPLATE_COMPACT = "Pages/AetherCore/Features/Permissions/AEGroupGraphNodeCompact.ui";

    private final String name;
    private final int memberCount;
    private final int priority;
    private final boolean compact;
    private final Runnable onClick;

    AEGroupGraphNode(String name, int memberCount, int priority, boolean compact, Runnable onClick) {
        this.name = name;
        this.memberCount = memberCount;
        this.priority = priority;
        this.compact = compact;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        return compact ? TEMPLATE_COMPACT : TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        String code = memberCount == 1 ? "ui.groups.node.detail_singular" : "ui.groups.node.detail_plural";
        String detail = lang(code, args("count", String.valueOf(memberCount), "priority", String.valueOf(priority)));
        return Map.of("Name", new AELabel(name), "Detail", new AELabel(detail), "OnActivate", onClick);
    }
}
