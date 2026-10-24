package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AEIconButton;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AETableRow;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Group Details' Perms sub-tab: a thin renderer over {@link AEUIGroupDetailsView}'s draft state -
 * the parent view, sub-tab name selector, and Save/Cancel footer all read/write through it, since
 * it (not this instance) survives sub-tab switches. Rows are the real {@link AEGroupPermission}
 * relation, not just the node string, so the Created At column reads {@code getCreatedAt()}
 * directly off the row already fetched for the table - no separate lookup. "+ Permissions" opens a
 * popup (suggesting nodes already granted elsewhere, but free text is never blocked); confirming it
 * stages every picked node into the parent's draft, shown green until the page's own Save is
 * pressed. Clicking a staged (not-yet-saved) row opens a popup where world/expires-in metadata can
 * be set; a persisted row with metadata opens the same popup read-only; a persisted row without
 * metadata has nothing to show and the click is a no-op. Deletion is never done from this popup -
 * it's checkbox-select-then-trash-button only (see {@link #batchDelete()}). Picks between two
 * template variants: the create-mode one adds a Name field, absent once the group already exists.
 */
final class AEUIGroupPermsSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupPermsSubView.ui";
    private static final String TEMPLATE_CREATE = "Pages/AetherCore/Features/Permissions/AEUIGroupPermsSubViewCreate.ui";

    private final AEUIGroupDetailsView parent;
    /** Owned here, not by {@link AETable} - a batch action rebuilds the table from scratch, so this is the only thing that survives across it. Only ever changed by an explicit checkbox click - see {@link AETable}'s own class doc. */
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    AEUIGroupPermsSubView(AEUIGroupDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return parent.isCreateMode() ? TEMPLATE_CREATE : TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        if (parent.isCreateMode()) {
            content.put("NameInput", new AETextInput(lang("ui.group.name_placeholder"), parent.getDraftName(), parent::setDraftName));
        }
        content.put("Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.permnode.add"), this::openAddDialog))));
        content.put("Table", new AETable<>("", getColumns(), lang("ui.permnode.empty"), this::fetch, this::openDetails, this::rowVariant,
                AEGroupPermission::getPermission, selectedKeys, this::onToggleSelect, List.of(
                        new AEIconButton(AEIconButton.Icon.TRASH, this::batchDelete, selectedKeys::isEmpty),
                        new AEIconButton(AEIconButton.Icon.UNDO, this::batchUndo, this::noneSelectedAreRemoved)
                ), true));
        return content;
    }

    private void onToggleSelect(String key, boolean checked) {
        if (checked) {
            selectedKeys.add(key);
        } else {
            selectedKeys.remove(key);
        }
    }

    /** Undo should only be clickable when it would actually do something - i.e. at least one CHECKED row is currently staged for removal, not merely "something is checked" (see the class doc on why checked rows outlive a batch action). */
    private boolean noneSelectedAreRemoved() {
        return selectedKeys.stream().noneMatch(parent::isDraftRemovedPermission);
    }

    // Created At uses a null-safe comparator: a staged (not-yet-saved) addition has no timestamp yet.
    private List<AEUIColDef<AEGroupPermission>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.permnode.col.permission"), AEGroupPermission::getPermission),
                new AEUIColDef<>(lang("ui.permnode.col.created_at"), relation -> AEDateUtils.formatByConfig(relation.getCreatedAt()),
                        Comparator.comparing(AEGroupPermission::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    /** Turns every selected permission's row red (or discards it outright if it was itself an unconfirmed addition) - the only way to delete from this table. Never touches {@link #selectedKeys} - a checkbox only ever changes from the admin's own click, see {@link AETable}'s class doc. */
    private void batchDelete() {
        selectedKeys.forEach(parent::removeDraftPermission);
        refresh("Table");
    }

    /** Reverts every selected permission's staged removal back to normal - checked rows stay checked afterwards, same reasoning as {@link #batchDelete()}. */
    private void batchUndo() {
        selectedKeys.forEach(parent::unremoveDraftPermission);
        refresh("Table");
    }

    private AEPageResult<AEGroupPermission> fetch(AEPageQuery query) {
        List<AEGroupPermission> all = parent.getDisplayedPermissions();
        List<AEGroupPermission> filtered = query.hasSearch()
                ? all.stream().filter(relation -> relation.getPermission().toLowerCase(Locale.ROOT).contains(query.search().toLowerCase(Locale.ROOT))).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    private AETableRow.Variant rowVariant(AEGroupPermission relation) {
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
    private void openDetails(AEGroupPermission relation) {
        String node = relation.getPermission();
        if (parent.isDraftAddedPermission(node)) {
            AEGroupPermissionMetadata staged = parent.getStagedPermissionMetadata(node);
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
        AEGroupPermissionMetadata metadata = resolveMetadata(node);
        if (hasMetadata(metadata)) {
            showPopup(new AEUIRowDetailsPopup(node, metadata.getWorldName(), metadata.getExpiresAt()));
        }
    }

    private AEGroupPermissionMetadata resolveMetadata(String node) {
        if (parent.isDraftAddedPermission(node)) {
            return parent.getStagedPermissionMetadata(node);
        }
        if (parent.isCreateMode() || parent.getGroup() == null) {
            return null;
        }
        return getPermissionsModule().getGroupsModule().getPermissionMetadata(parent.getGroup().getName(), node);
    }

    private static boolean hasMetadata(AEGroupPermissionMetadata metadata) {
        return metadata != null && (metadata.getWorldName() != null || metadata.getExpiresAt() != null);
    }

    private void openAddDialog() {
        Set<String> excluded = parent.getDisplayedPermissions().stream().map(AEGroupPermission::getPermission).collect(Collectors.toUnmodifiableSet());
        showPopup(new AEUIAddPermissionsPopup(lang("ui.permnode.popup_title"), excluded, nodes -> {
            nodes.forEach(parent::addDraftPermission);
            refresh("Table");
        }));
    }

    private AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }
}
