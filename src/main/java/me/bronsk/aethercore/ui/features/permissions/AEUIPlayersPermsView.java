package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.features.players.AEUIPlayersTable;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/** Players Perms sub-tab: the same players table as the Players tab, but rows lead to the permissions detail view instead. */
final class AEUIPlayersPermsView extends AEUIComponent {

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", new AEUIPlayersTable(this::openDetails));
    }

    private void openDetails(AEPlayer player) {
        navigate(new AEUIPlayerPermsDetailsView(player));
    }
}
