package me.bronsk.aethercore.ui.features.chat;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.util.List;
import java.util.Map;

/** The Chat tab: just the shell + sub-tab switch between Groups/Players. */
public final class AEUIChatView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Shells/SubTabsWithTitleShell.ui";

    private enum SubTab { GROUPS, PLAYERS }

    private final AEUITabs subTabs;

    public AEUIChatView() {
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.chat.groups", SubTab.GROUPS, AEUIChatGroupsView::new),
                new AEUITabDef("ui.tab.chat.players", SubTab.PLAYERS, AEUIChatPlayersView::new)
        ), SubTab.GROUPS, false, this::onSubTabChanged);
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.Main", new AELabel(lang("ui.chat.title")),
                "Title.Id", List.of(),
                "SubTabs", subTabs,
                "Details", subTabs.current()
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }
}
