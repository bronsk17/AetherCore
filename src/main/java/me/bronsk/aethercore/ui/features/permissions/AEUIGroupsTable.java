package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The standard groups table: Name/Priority/Members/Created At, sortable, defaulting to Priority
 * descending - used everywhere a table of {@link AEGroup} rows shows up (Permissions' own Groups
 * table, Chat's Groups table, ...) so they can't drift apart into showing different columns for the
 * same underlying data. No template of its own - binds its one {@link AETable} child directly onto
 * whatever slot it's placed in, same as embedding the table there directly.
 */
public final class AEUIGroupsTable extends AEUIComponent {

    private static final Comparator<AEGroup> DEFAULT_SORT = Comparator.comparingInt(AEUIGroupsTable::priorityValue).reversed();

    private final Consumer<AEGroup> onRowClick;

    public AEUIGroupsTable(Consumer<AEGroup> onRowClick) {
        this.onRowClick = onRowClick;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", new AETable<>("", getColumns(), lang("ui.groups.empty"), this::fetch, onRowClick, true, DEFAULT_SORT));
    }

    private List<AEUIColDef<AEGroup>> getColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.groups.col.name"), AEGroup::getName),
                new AEUIColDef<>(lang("ui.groups.col.priority"), AEUIGroupsTable::formatPriority, Comparator.comparingInt(AEUIGroupsTable::priorityValue)),
                new AEUIColDef<>(lang("ui.groups.col.members"), AEUIGroupsTable::formatMemberCount, Comparator.comparingInt(AEUIGroupsTable::memberCountValue)),
                new AEUIColDef<>(lang("ui.groups.col.created_at"), group -> AEDateUtils.formatByConfig(group.getCreatedAt()), Comparator.comparing(AEGroup::getCreatedAt))
        );
    }

    private AEPageResult<AEGroup> fetch(AEPageQuery query) {
        return getPermissionsModule().getGroupsModule().getGroupQueryResult(query);
    }

    private static String formatPriority(AEGroup group) {
        return String.valueOf(priorityValue(group));
    }

    private static int priorityValue(AEGroup group) {
        Integer priority = getPermissionsModule().getGroupSettingsModule().getGroupPriority(group.getName());
        return priority == null ? 0 : priority;
    }

    private static String formatMemberCount(AEGroup group) {
        return String.valueOf(memberCountValue(group));
    }

    private static int memberCountValue(AEGroup group) {
        return getPermissionsModule().getGroupsModule().getGroupMemberCount(group.getName());
    }

    private static AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }
}
