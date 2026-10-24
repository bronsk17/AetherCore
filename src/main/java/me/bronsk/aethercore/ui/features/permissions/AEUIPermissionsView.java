package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.util.List;
import java.util.Map;

/** The Permissions tab: just the shell + sub-tab switch between Groups/Players. */
public final class AEUIPermissionsView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Shells/SubTabsWithTitleShell.ui";

    private enum SubTab { GROUPS, PLAYERS }

    private final AEUITabs subTabs;

    public AEUIPermissionsView() {
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.permissions.groups", SubTab.GROUPS, AEUIGroupsPermsView::new),
                new AEUITabDef("ui.tab.permissions.players", SubTab.PLAYERS, AEUIPlayersPermsView::new)
        ), SubTab.GROUPS, false, this::onSubTabChanged);
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.Main", new AELabel(lang("ui.permissions.title")),
                "Title.Id", List.of(),
                "SubTabs", subTabs,
                "Details", subTabs.current()
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }
}
