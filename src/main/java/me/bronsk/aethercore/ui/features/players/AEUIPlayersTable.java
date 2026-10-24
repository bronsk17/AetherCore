package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The standard registered-players table: Name/UUID/Created At/Updated At, sortable - used everywhere
 * a table of {@link AEPlayer} rows shows up (the Players tab, Players Perms, Chat's Players tab, ...)
 * so they can't drift apart into showing different columns for the same underlying data. No template
 * of its own - binds its one {@link AETable} child directly onto whatever slot it's placed in, same
 * as embedding the table there directly.
 */
public final class AEUIPlayersTable extends AEUIComponent {

    private final Consumer<AEPlayer> onRowClick;

    public AEUIPlayersTable(Consumer<AEPlayer> onRowClick) {
        this.onRowClick = onRowClick;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", new AETable<>("", getColumns(), lang("ui.players.empty"), this::fetch, onRowClick, true));
    }

    private List<AEUIColDef<AEPlayer>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.players.col.name"), AEUIPlayersTable::getDisplayName),
                new AEUIColDef<>(lang("ui.players.col.uuid"), AEPlayer::getUuid),
                new AEUIColDef<>(lang("ui.players.col.created_at"), player -> AEDateUtils.formatByConfig(player.getCreatedAt()), Comparator.comparing(AEPlayer::getCreatedAt)),
                new AEUIColDef<>(lang("ui.players.col.updated_at"), player -> AEDateUtils.formatByConfig(player.getUpdatedAt()), Comparator.comparing(AEPlayer::getUpdatedAt))
        );
    }

    private AEPageResult<AEPlayer> fetch(AEPageQuery query) {
        return AEModulesManager.get().getModule(AEPlayersModule.class).getPlayerQueryResult(query);
    }

    static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }
}
