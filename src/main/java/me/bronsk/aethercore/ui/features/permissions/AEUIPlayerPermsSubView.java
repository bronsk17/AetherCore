package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AEIconButton;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AETableRow;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Player Perms Details' Player Perms sub-tab: same staged/checkbox-select-then-trash pattern as
 * the Group's Perms tab (see {@link AEUIGroupPermsSubView}) - deletion is checkbox+trash/undo
 * only, never a per-row popup button. Rows are the real {@link AEPlayerPermission} relation, not
 * just the node string, so the Created At column reads {@code getCreatedAt()} directly off the row
 * already fetched for the table - no separate lookup. Metadata (world/expires-in) can only be set
 * while a permission is still a staged (not-yet-saved) addition - its popup offers editable
 * world/expires fields that stage into the parent's draft; a persisted grant with metadata opens
 * the same popup read-only; one without metadata has nothing to show and the click is a no-op.
 */
final class AEUIPlayerPermsSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIPlayerPermsSubView.ui";

    private final AEUIPlayerPermsDetailsView parent;
    /** Owned here, not by {@link AETable} - see {@link AEUIGroupPermsSubView}'s field of the same name for why. */
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    AEUIPlayerPermsSubView(AEUIPlayerPermsDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        AETable<AEPlayerPermission> table = new AETable<>("", getColumns(), lang("ui.permnode.empty"), this::fetch, this::openDetails, this::rowVariant,
                AEPlayerPermission::getPermission, selectedKeys, this::onToggleSelect, List.of(
                        new AEIconButton(AEIconButton.Icon.TRASH, this::batchDelete, selectedKeys::isEmpty),
                        new AEIconButton(AEIconButton.Icon.UNDO, this::batchUndo, this::noneSelectedAreRemoved)
                ), true);
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.permnode.add"), this::openAddDialog))),
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
        return selectedKeys.stream().noneMatch(parent::isDraftRemovedPermission);
    }

    // Created At uses a null-safe comparator: a staged (not-yet-saved) addition has no timestamp yet.
    private List<AEUIColDef<AEPlayerPermission>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.permnode.col.permission"), this::formatPermission),
                new AEUIColDef<>(lang("ui.permnode.col.created_at"), relation -> AEDateUtils.formatByConfig(relation.getCreatedAt()),
                        Comparator.comparing(AEPlayerPermission::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    /** Turns every selected permission's row red (or discards it outright if it was itself an unconfirmed addition) - the only way to delete from this table. Never touches {@link #selectedKeys}. */
    private void batchDelete() {
        selectedKeys.forEach(parent::removeDraftPermission);
        refresh("Table");
    }

    /** Reverts every selected permission's staged removal back to normal - checked rows stay checked afterwards. */
    private void batchUndo() {
        selectedKeys.forEach(parent::unremoveDraftPermission);
        refresh("Table");
    }

    private AEPageResult<AEPlayerPermission> fetch(AEPageQuery query) {
        List<AEPlayerPermission> all = parent.getDisplayedPermissions();
        List<AEPlayerPermission> filtered = query.hasSearch()
                ? all.stream().filter(relation -> relation.getPermission().toLowerCase(Locale.ROOT).contains(query.search().toLowerCase(Locale.ROOT))).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    private AETableRow.Variant rowVariant(AEPlayerPermission relation) {
        if (parent.isDraftAddedPermission(relation.getPermission())) {
            return AETableRow.Variant.ADDED;
        }
        if (parent.isDraftRemovedPermission(relation.getPermission())) {
            return AETableRow.Variant.REMOVED;
        }
        return AETableRow.Variant.NORMAL;
    }

    /**
     * Metadata (world/expires-in) can only be set while the permission is still a staged addition -
     * once persisted (Save pressed), it's immutable: the popup shows it read-only, matching the
     * "remove and re-add to change it" rule. A persisted permission with no metadata has nothing to
     * show, so the click does nothing.
     */
    private void openDetails(AEPlayerPermission relation) {
        String node = relation.getPermission();
        if (parent.isDraftAddedPermission(node)) {
            AEUserPermissionMetadata staged = parent.getStagedPermissionMetadata(node);
            showPopup(new AEUIRowDetailsPopup(
                    node,
                    staged == null ? null : staged.getWorldName(),
                    staged == null ? null : staged.getExpiresInText(),
                    (worldName, expiresAt, expiresInText) -> {
                        parent.stagePermissionMetadata(node, worldName, expiresAt, expiresInText);
                        refresh("Table");
                    }
            ));
            return;
        }
        AEUserPermissionMetadata metadata = resolveMetadata(node);
        if (hasMetadata(metadata)) {
            showPopup(new AEUIRowDetailsPopup(node, metadata.getWorldName(), metadata.getExpiresAt()));
        }
    }

    private String formatPermission(AEPlayerPermission relation) {
        String node = relation.getPermission();
        return hasMetadata(resolveMetadata(node)) ? node + " [C]" : node;
    }

    private AEUserPermissionMetadata resolveMetadata(String node) {
        if (parent.isDraftAddedPermission(node)) {
            return parent.getStagedPermissionMetadata(node);
        }
        return parent.getPermissionsModule().getUsersModule().getPermissionMetadata(parent.playerUuid(), node);
    }

    private static boolean hasMetadata(AEUserPermissionMetadata metadata) {
        return metadata != null && (metadata.getWorldName() != null || metadata.getExpiresAt() != null);
    }

    private void openAddDialog() {
        Set<String> excluded = parent.getDisplayedPermissions().stream().map(AEPlayerPermission::getPermission).collect(Collectors.toUnmodifiableSet());
        showPopup(new AEUIAddPermissionsPopup(lang("ui.permnode.popup_title"), excluded, nodes -> {
            nodes.forEach(parent::addDraftPermission);
            refresh("Table");
        }));
    }
}
