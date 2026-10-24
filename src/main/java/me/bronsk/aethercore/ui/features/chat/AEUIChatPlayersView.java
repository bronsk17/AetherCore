package me.bronsk.aethercore.ui.features.chat;

import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.features.players.AEUIPlayersTable;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;
import java.util.UUID;

/** Chat Players sub-tab: every registered player, click a row to open its chat settings. Same {@link AEUIPlayersTable} as the Players tab. */
final class AEUIChatPlayersView extends AEUIComponent {

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", new AEUIPlayersTable(this::openDetails));
    }

    private void openDetails(AEPlayer player) {
        navigate(new AEUIChatPlayerSettingsView(UUID.fromString(player.getUuid()), getDisplayName(player)));
    }

    private static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }
}
