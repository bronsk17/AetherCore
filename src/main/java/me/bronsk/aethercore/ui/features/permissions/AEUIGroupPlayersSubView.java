package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AEIconButton;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AETableRow;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Group Details' Players sub-tab: same staged pattern as Perms/Children - a thin renderer over
 * {@link AEUIGroupDetailsView}'s draft state. Rows pair the resolved {@link AEPlayer} with when
 * their membership was created, both already fetched together by the parent - no separate lookup
 * for the Created At column. Metadata (world/expires-in) can only be set while a membership is
 * still a staged (not-yet-saved) addition - its popup offers editable world/expires fields that
 * stage into the parent's draft; a persisted membership with metadata opens the same popup
 * read-only, one without metadata has nothing to show and the click is a no-op. Deletion is never
 * done from this popup - it's checkbox-select-then-trash-button only (see {@link #batchDelete()}).
 */
final class AEUIGroupPlayersSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupPlayersSubView.ui";

    private final AEUIGroupDetailsView parent;
    /** Owned here, not by {@link AETable} - see {@link AEUIGroupPermsSubView}'s field of the same name for why. */
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    AEUIGroupPlayersSubView(AEUIGroupDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        AETable<Map.Entry<AEPlayer, Timestamp>> table = new AETable<>("", getColumns(), lang("ui.group.players.empty"), this::fetch, this::openDetails, this::rowVariant,
                entry -> entry.getKey().getUuid(), selectedKeys, this::onToggleSelect, List.of(
                        new AEIconButton(AEIconButton.Icon.TRASH, this::batchDelete, selectedKeys::isEmpty),
                        new AEIconButton(AEIconButton.Icon.UNDO, this::batchUndo, this::noneSelectedAreRemoved)
                ), true);
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.group.players.add"), this::openAddDialog))),
                "Table", table
        );
    }

    private void onToggleSelect(String key, boolean checked) {
        if (checked) {
            selectedKeys.add(key);
        } else {
            selectedKeys.remove(key);
        }
    }

    /** Undo should only be clickable when it would actually do something - see {@link AEUIGroupPermsSubView}'s method of the same name. */
    private boolean noneSelectedAreRemoved() {
        return selectedKeys.stream().noneMatch(parent::isDraftRemovedPlayer);
    }

    // Created At uses a null-safe comparator: a staged (not-yet-saved) addition has no timestamp yet.
    private List<AEUIColDef<Map.Entry<AEPlayer, Timestamp>>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.group.players.col.name"), entry -> getDisplayName(entry.getKey()), Comparator.comparing(entry -> getDisplayName(entry.getKey()))),
                new AEUIColDef<>(lang("ui.group.players.col.uuid"), this::formatUuid, Comparator.comparing(entry -> entry.getKey().getUuid())),
                new AEUIColDef<>(lang("ui.group.players.col.created_at"), entry -> AEDateUtils.formatByConfig(entry.getValue()),
                        Comparator.comparing(Map.Entry::getValue, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    /** Turns every selected player's row red (or discards it outright if it was itself an unconfirmed addition) - the only way to delete from this table. Never touches {@link #selectedKeys}. */
    private void batchDelete() {
        selectedKeys.forEach(parent::removeDraftPlayer);
        refresh("Table");
    }

    /** Reverts every selected player's staged removal back to normal - checked rows stay checked afterwards. */
    private void batchUndo() {
        selectedKeys.forEach(parent::unremoveDraftPlayer);
        refresh("Table");
    }

    private AEPageResult<Map.Entry<AEPlayer, Timestamp>> fetch(AEPageQuery query) {
        List<Map.Entry<AEPlayer, Timestamp>> all = List.copyOf(parent.getDisplayedPlayers().values());
        List<Map.Entry<AEPlayer, Timestamp>> filtered = query.hasSearch()
                ? all.stream().filter(entry -> matches(entry.getKey(), query.search())).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    private boolean matches(AEPlayer player, String search) {
        String needle = search.toLowerCase(Locale.ROOT);
        return getDisplayName(player).toLowerCase(Locale.ROOT).contains(needle) || player.getUuid().contains(search);
    }

    private AETableRow.Variant rowVariant(Map.Entry<AEPlayer, Timestamp> entry) {
        String uuid = entry.getKey().getUuid();
        if (parent.isDraftAddedPlayer(uuid)) {
            return AETableRow.Variant.ADDED;
        }
        if (parent.isDraftRemovedPlayer(uuid)) {
            return AETableRow.Variant.REMOVED;
        }
        return AETableRow.Variant.NORMAL;
    }

    private void openDetails(Map.Entry<AEPlayer, Timestamp> entry) {
        AEPlayer player = entry.getKey();
        String title = getDisplayName(player);
        if (parent.isDraftAddedPlayer(player.getUuid())) {
            AEPlayerGroupMetadata staged = parent.getStagedPlayerMetadata(player.getUuid());
            showPopup(new AEUIRowDetailsPopup(
                    title,
                    staged == null ? null : staged.getWorldName(),
                    staged == null ? null : staged.getExpiresInText(),
                    (worldName, expiresAt, expiresInText) -> {
                        parent.stagePlayerMetadata(player.getUuid(), worldName, expiresAt, expiresInText);
                        refresh("Table");
                    }
            ));
            return;
        }
        AEPlayerGroupMetadata metadata = resolveMetadata(player);
        if (hasMetadata(metadata)) {
            showPopup(new AEUIRowDetailsPopup(title, metadata.getWorldName(), metadata.getExpiresAt()));
        }
    }

    private void openAddDialog() {
        Set<String> excluded = new HashSet<>(parent.getDisplayedPlayers().keySet());
        showPopup(new AEUIAddPlayersToGroupPopup(lang("ui.group.players.popup_title"), excluded, players -> {
            players.forEach(parent::addDraftPlayer);
            refresh("Table");
        }));
    }

    private String formatUuid(Map.Entry<AEPlayer, Timestamp> entry) {
        AEPlayer player = entry.getKey();
        return hasMetadata(resolveMetadata(player)) ? player.getUuid() + " [C]" : player.getUuid();
    }

    private AEPlayerGroupMetadata resolveMetadata(AEPlayer player) {
        if (parent.isDraftAddedPlayer(player.getUuid())) {
            return parent.getStagedPlayerMetadata(player.getUuid());
        }
        if (parent.isCreateMode() || parent.getGroup() == null) {
            return null;
        }
        return getPermissionsModule().getUsersModule().getGroupMetadata(UUID.fromString(player.getUuid()), parent.getGroup().getName());
    }

    private static boolean hasMetadata(AEPlayerGroupMetadata metadata) {
        return metadata != null && (metadata.getWorldName() != null || metadata.getExpiresAt() != null);
    }

    private AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }

    private static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }
}
