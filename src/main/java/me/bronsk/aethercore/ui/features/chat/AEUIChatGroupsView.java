package me.bronsk.aethercore.ui.features.chat;

import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.ui.features.permissions.AEUIGroupsTable;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** Chat Groups sub-tab: every registered group, click a row to open its chat settings. Same {@link AEUIGroupsTable} as Permissions' own Groups table. */
final class AEUIChatGroupsView extends AEUIComponent {

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", new AEUIGroupsTable(this::openDetails));
    }

    private void openDetails(AEGroup group) {
        navigate(new AEUIChatGroupSettingsView(group.getName()));
    }
}
