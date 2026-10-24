package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AELabel;

import java.util.Map;

/** The Players tab: a server-paginated table of every registered player, click a row to open its details. */
public final class AEUIPlayersView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Shells/TitledContentShell.ui";

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title", new AELabel(lang("ui.players.title")),
                "Content", new AEUIPlayersTable(this::openDetails)
        );
    }

    private void openDetails(AEPlayer player) {
        navigate(new AEUIPlayerDetailsView(player));
    }
}
