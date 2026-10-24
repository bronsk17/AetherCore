package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Player Perms Details' Player Groups sub-tab: a thin renderer over {@link
 * AEUIPlayerPermsDetailsView}'s draft state, same checkbox-select-then-trash pattern as the Perms
 * sub-tab (see {@link AEUIGroupPermsSubView}) - deletion is checkbox+trash/undo only, never a
 * per-row popup button. This edits the same underlying {@code AEPlayerGroup} relation as the
 * Group's own Players tab. Rows pair the group name with when the membership was created, both
 * already fetched together by the parent - no separate lookup for the Created At column. Metadata
 * (world/expires-in) can only be set while a membership is still a staged (not-yet-saved) addition
 * - once Save persists it, it's immutable: the popup shows it read-only, and changing it means
 * removing and re-adding the group.
 */
final class AEUIPlayerGroupsSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIPlayerGroupsSubView.ui";

    private final AEUIPlayerPermsDetailsView parent;
    /** Owned here, not by {@link AETable} - see {@link AEUIGroupPermsSubView}'s field of the same name for why. */
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    AEUIPlayerGroupsSubView(AEUIPlayerPermsDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        AETable<Map.Entry<String, Timestamp>> table = new AETable<>("", getColumns(), lang("ui.playerperms.groups.empty"), this::fetch, this::openDetails, this::rowVariant,
                Map.Entry::getKey, selectedKeys, this::onToggleSelect, List.of(
                        new AEIconButton(AEIconButton.Icon.TRASH, this::batchDelete, selectedKeys::isEmpty),
                        new AEIconButton(AEIconButton.Icon.UNDO, this::batchUndo, this::noneSelectedAreRemoved)
                ), true);
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.playerperms.groups.add"), this::openAddDialog))),
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
        return selectedKeys.stream().noneMatch(parent::isDraftRemovedGroup);
    }

    // Created At uses a null-safe comparator: a staged (not-yet-saved) addition has no timestamp yet.
    private List<AEUIColDef<Map.Entry<String, Timestamp>>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.playerperms.groups.col.group"), this::formatGroupName, Comparator.comparing(Map.Entry::getKey)),
                new AEUIColDef<>(lang("ui.playerperms.groups.col.created_at"), entry -> AEDateUtils.formatByConfig(entry.getValue()),
                        Comparator.comparing(Map.Entry::getValue, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    /** Turns every selected group's row red (or discards it outright if it was itself an unconfirmed addition) - the only way to delete from this table. Never touches {@link #selectedKeys}. */
    private void batchDelete() {
        selectedKeys.forEach(parent::removeDraftGroup);
        refresh("Table");
    }

    /** Reverts every selected group's staged removal back to normal - checked rows stay checked afterwards. */
    private void batchUndo() {
        selectedKeys.forEach(parent::unremoveDraftGroup);
        refresh("Table");
    }

    private AEPageResult<Map.Entry<String, Timestamp>> fetch(AEPageQuery query) {
        List<Map.Entry<String, Timestamp>> all = parent.getDisplayedGroups().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .toList();
        List<Map.Entry<String, Timestamp>> filtered = query.hasSearch()
                ? all.stream().filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).contains(query.search().toLowerCase(Locale.ROOT))).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    private AETableRow.Variant rowVariant(Map.Entry<String, Timestamp> entry) {
        String groupName = entry.getKey();
        if (parent.isDraftAddedGroup(groupName)) {
            return AETableRow.Variant.ADDED;
        }
        if (parent.isDraftRemovedGroup(groupName)) {
            return AETableRow.Variant.REMOVED;
        }
        return AETableRow.Variant.NORMAL;
    }

    /**
     * Metadata (world/expires-in) can only be set while the membership is still a staged addition -
     * once persisted (Save pressed), it's immutable: the popup shows it read-only, matching the
     * "remove and re-add to change it" rule. A persisted membership with no metadata has nothing to
     * show, so the click does nothing.
     */
    private void openDetails(Map.Entry<String, Timestamp> entry) {
        String groupName = entry.getKey();
        if (parent.isDraftAddedGroup(groupName)) {
            AEPlayerGroupMetadata staged = parent.getStagedGroupMetadata(groupName);
            showPopup(new AEUIRowDetailsPopup(
                    groupName,
                    staged == null ? null : staged.getWorldName(),
                    staged == null ? null : staged.getExpiresInText(),
                    (worldName, expiresAt, expiresInText) -> {
                        parent.stageGroupMetadata(groupName, worldName, expiresAt, expiresInText);
                        refresh("Table");
                    }
            ));
            return;
        }
        AEPlayerGroupMetadata metadata = resolveMetadata(groupName);
        if (hasMetadata(metadata)) {
            showPopup(new AEUIRowDetailsPopup(groupName, metadata.getWorldName(), metadata.getExpiresAt()));
        }
    }

    private String formatGroupName(Map.Entry<String, Timestamp> entry) {
        String groupName = entry.getKey();
        return hasMetadata(resolveMetadata(groupName)) ? groupName + " [C]" : groupName;
    }

    private AEPlayerGroupMetadata resolveMetadata(String groupName) {
        if (parent.isDraftAddedGroup(groupName)) {
            return parent.getStagedGroupMetadata(groupName);
        }
        return parent.getPermissionsModule().getUsersModule().getGroupMetadata(parent.playerUuid(), groupName);
    }

    private static boolean hasMetadata(AEPlayerGroupMetadata metadata) {
        return metadata != null && (metadata.getWorldName() != null || metadata.getExpiresAt() != null);
    }

    private void openAddDialog() {
        showPopup(new AEUIAddGroupsPopup(lang("ui.playerperms.groups.popup_title"), Set.copyOf(parent.getDisplayedGroups().keySet()), groups -> {
            groups.forEach(parent::addDraftGroup);
            refresh("Table");
        }));
    }
}
