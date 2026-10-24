package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsGroupsModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.ui.components.AEEmptyMessage;
import me.bronsk.aethercore.ui.components.AEPagination;
import me.bronsk.aethercore.ui.components.AEPlainGroup;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Groups Perms' "Grid" view: the group inheritance DAG broken into independent chains ({@link
 * AEGroupChainLayout}), one chain per page - there's no native horizontal scroll in this UI
 * framework (confirmed against the base game's own asset pack: every {@code ScrollbarStyle} usage
 * pairs with {@code LayoutMode: TopScrolling}, no horizontal variant exists), so cramming every
 * chain into one wide canvas isn't an option. Columns run left-to-right, root(s) first. A chain
 * longer than {@link #COMPACT_THRESHOLD_COLUMNS} columns renders every node with the compact
 * template so it still fits the panel width.
 */
final class AEUIGroupsGraphView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupsGraphView.ui";
    private static final int COMPACT_THRESHOLD_COLUMNS = 6;

    private int pageIndex = 0;

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<AEGroupChainLayout.Chain> chains = AEGroupChainLayout.computeChains(getAllGroupNames(), getChildrenOf());
        if (chains.isEmpty()) {
            return Map.of("Chain", List.of(new AEEmptyMessage(lang("ui.groups.empty"))), "Pagination", List.of());
        }

        pageIndex = Math.max(0, Math.min(pageIndex, chains.size() - 1));
        AEGroupChainLayout.Chain chain = chains.get(pageIndex);

        return Map.of(
                "Chain", List.of(renderChain(chain)),
                "Pagination", List.of(new AEPagination(pageIndex, 1, chains.size(), chains.size(), List.of(1), this::onPageChange, size -> { }))
        );
    }

    private void onPageChange(int newIndex) {
        pageIndex = newIndex;
        refresh("Chain", "Pagination");
    }

    private AEUIComponent renderChain(AEGroupChainLayout.Chain chain) {
        boolean compact = chain.columnCount() > COMPACT_THRESHOLD_COLUMNS;
        List<AEUIComponent> columns = chain.columns().stream()
                .map(column -> (AEUIComponent) new AEGroupChainColumn(renderNodes(column, compact)))
                .toList();
        return new AEPlainGroup("Left", columns);
    }

    private List<AEUIComponent> renderNodes(List<String> groupNames, boolean compact) {
        return groupNames.stream()
                .map(name -> (AEUIComponent) new AEGroupGraphNode(name, getMemberCount(name), getPriority(name), compact, () -> openDetails(name)))
                .toList();
    }

    private void openDetails(String groupName) {
        navigate(new AEUIGroupDetailsView(getGroupsModule().getGroup(groupName)));
    }

    private Set<String> getAllGroupNames() {
        return getGroupsModule().getAllRegisteredGroups();
    }

    private Map<String, Set<String>> getChildrenOf() {
        Map<String, Set<String>> childrenOf = new HashMap<>();
        for (String groupName : getAllGroupNames()) {
            childrenOf.put(groupName, getGroupsModule().getGroupChildren(groupName));
        }
        return childrenOf;
    }

    private int getMemberCount(String groupName) {
        return getGroupsModule().getGroupMemberCount(groupName);
    }

    private int getPriority(String groupName) {
        Integer priority = getPermissionsModule().getGroupSettingsModule().getGroupPriority(groupName);
        return priority == null ? 0 : priority;
    }

    private AEPermissionsGroupsModule getGroupsModule() {
        return getPermissionsModule().getGroupsModule();
    }

    private AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }
}
