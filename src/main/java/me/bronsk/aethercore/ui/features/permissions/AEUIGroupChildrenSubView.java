package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
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

/**
 * Group Details' Children sub-tab: which other registered groups this group inherits from - a
 * thin renderer over {@link AEUIGroupDetailsView}'s draft state, same pattern as the Perms
 * sub-tab. Rows are the (child name, edge created-at) pair the parent already fetched in one query
 * - no separate lookup for the Created At column. "+ Children" opens a popup limited to existing
 * groups (unlike permissions, a child must be a real group); confirming it stages every picked
 * group into the parent's draft, shown green until the page's own Save is pressed. There's no
 * per-row metadata to show here, so rows aren't clickable - deletion is checkbox-select-then-
 * trash-button only (see {@link #batchDelete()}).
 */
final class AEUIGroupChildrenSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupChildrenSubView.ui";

    private final AEUIGroupDetailsView parent;
    /** Owned here, not by {@link AETable} - see {@link AEUIGroupPermsSubView}'s field of the same name for why. */
    private final Set<String> selectedKeys = new LinkedHashSet<>();

    AEUIGroupChildrenSubView(AEUIGroupDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        // onRowClick is a no-op, not null: AETableRow only picks its ADDED/REMOVED tinted template
        // when it has a click handler at all, so a real (do-nothing) callback keeps the staged-color
        // feedback working even though there's nothing to open here.
        AETable<Map.Entry<String, Timestamp>> table = new AETable<>("", getColumns(), lang("ui.group.children.empty"), this::fetch, entry -> { }, this::rowVariant,
                entry -> entry.getKey(), selectedKeys, this::onToggleSelect, List.of(
                        new AEIconButton(AEIconButton.Icon.TRASH, this::batchDelete, selectedKeys::isEmpty),
                        new AEIconButton(AEIconButton.Icon.UNDO, this::batchUndo, this::noneSelectedAreRemoved)
                ), true);
        return Map.of(
                "Buttons", new AEButtonBar(List.of(new AEActionButton(lang("ui.group.children.add"), this::openAddDialog))),
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
        return selectedKeys.stream().noneMatch(parent::isDraftRemovedChild);
    }

    // Created At uses a null-safe comparator: a staged (not-yet-saved) addition has no timestamp yet.
    private List<AEUIColDef<Map.Entry<String, Timestamp>>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.group.children.col.group"), Map.Entry::getKey),
                new AEUIColDef<>(lang("ui.group.children.col.created_at"), entry -> AEDateUtils.formatByConfig(entry.getValue()),
                        Comparator.comparing(Map.Entry::getValue, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    /** Turns every selected child's row red (or discards it outright if it was itself an unconfirmed addition) - the only way to delete from this table. Never touches {@link #selectedKeys}. */
    private void batchDelete() {
        selectedKeys.forEach(parent::removeDraftChild);
        refresh("Table");
    }

    /** Reverts every selected child's staged removal back to normal - checked rows stay checked afterwards. */
    private void batchUndo() {
        selectedKeys.forEach(parent::unremoveDraftChild);
        refresh("Table");
    }

    private AEPageResult<Map.Entry<String, Timestamp>> fetch(AEPageQuery query) {
        List<Map.Entry<String, Timestamp>> all = List.copyOf(parent.getDisplayedChildren().entrySet());
        List<Map.Entry<String, Timestamp>> filtered = query.hasSearch()
                ? all.stream().filter(entry -> entry.getKey().toLowerCase(Locale.ROOT).contains(query.search().toLowerCase(Locale.ROOT))).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    private AETableRow.Variant rowVariant(Map.Entry<String, Timestamp> entry) {
        String childName = entry.getKey();
        if (parent.isDraftAddedChild(childName)) {
            return AETableRow.Variant.ADDED;
        }
        if (parent.isDraftRemovedChild(childName)) {
            return AETableRow.Variant.REMOVED;
        }
        return AETableRow.Variant.NORMAL;
    }

    private void openAddDialog() {
        Set<String> excluded = new HashSet<>(parent.getDisplayedChildren().keySet());
        if (parent.getGroup() != null) {
            excluded.add(parent.getGroup().getName());
        }
        showPopup(new AEUIAddGroupsPopup(lang("ui.group.children.popup_title"), excluded, groups -> {
            groups.forEach(parent::addDraftChild);
            refresh("Table");
        }));
    }
}
