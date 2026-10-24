package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.util.List;
import java.util.Map;

/**
 * Groups Perms sub-tab: every registered group, click a row to open its details; "+ Group" sits on
 * the same left-aligned bar as the Table/Grid toggle. That toggle switches between {@link
 * AEUIGroupsTable} (shared with Chat's own Groups table so both show the same columns) and {@link
 * AEUIGroupsGraphView}, which draws the inheritance chains instead.
 */
final class AEUIGroupsPermsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupsPermsView.ui";

    private enum ViewMode { TABLE, GRID }

    private final AEUITabs viewTabs;

    AEUIGroupsPermsView() {
        this.viewTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.groups.table", ViewMode.TABLE, () -> new AEUIGroupsTable(this::openDetails)),
                new AEUITabDef("ui.tab.groups.grid", ViewMode.GRID, AEUIGroupsGraphView::new)
        ), ViewMode.TABLE, false, this::onViewChanged);
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Bar.ViewTabs", viewTabs,
                "Bar.AddButton", new AEActionButton(lang("ui.groups.add"), this::openCreateNew),
                "Content", viewTabs.current()
        );
    }

    private void onViewChanged() {
        refresh("Bar.ViewTabs", "Content");
    }

    private void openDetails(AEGroup group) {
        navigate(new AEUIGroupDetailsView(group));
    }

    private void openCreateNew() {
        navigate(AEUIGroupDetailsView.createNew());
    }
}
