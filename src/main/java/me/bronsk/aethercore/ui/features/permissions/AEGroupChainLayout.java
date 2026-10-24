package me.bronsk.aethercore.ui.features.permissions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Pure layout math for the Groups Perms "Grid" view: breaks the group inheritance DAG into
 * independent chains (weakly-connected components) and, within each chain, assigns every group a
 * column (generation - root(s) leftmost, less-privileged groups further right) and a row (vertical
 * slot, ordered so a node with several parents lands centered among them via barycenter ordering).
 * No UI/rendering concerns - {@link AEUIGroupsGraphView} turns the result into boxes. Safe against
 * infinite loops because the inheritance graph is cycle-free by construction (enforced in {@link
 * me.bronsk.aethercore.modules.permissions.storage.AEGroupsPermissionsSqlStorage#addGroupChild}).
 */
final class AEGroupChainLayout {

    /** One inheritance chain: columns left-to-right (root(s) first), each column top-to-bottom. */
    record Chain(List<List<String>> columns) {
        int columnCount() {
            return columns.size();
        }
    }

    private AEGroupChainLayout() {
    }

    /**
     * Splits every registered group into independent chains, ordered deterministically (by the
     * name of each chain's first root).
     *
     * @param allGroups Every registered group name.
     * @param childrenOf Direct "inherits from" edges: group name -> the group names it inherits from.
     */
    static List<Chain> computeChains(Set<String> allGroups, Map<String, Set<String>> childrenOf) {
        Map<String, Set<String>> parentsOf = buildParentsOf(allGroups, childrenOf);
        List<Set<String>> components = findConnectedComponents(allGroups, childrenOf, parentsOf);

        List<Chain> chains = new ArrayList<>();
        for (Set<String> component : components) {
            chains.add(layoutComponent(component, childrenOf, parentsOf));
        }
        chains.sort(Comparator.comparing(chain -> chain.columns().get(0).get(0)));
        return chains;
    }

    private static Map<String, Set<String>> buildParentsOf(Set<String> allGroups, Map<String, Set<String>> childrenOf) {
        Map<String, Set<String>> parentsOf = new HashMap<>();
        for (String group : allGroups) {
            parentsOf.put(group, new LinkedHashSet<>());
        }
        for (String group : allGroups) {
            for (String child : childrenOf.getOrDefault(group, Set.of())) {
                parentsOf.computeIfAbsent(child, key -> new LinkedHashSet<>()).add(group);
            }
        }
        return parentsOf;
    }

    private static List<Set<String>> findConnectedComponents(Set<String> allGroups, Map<String, Set<String>> childrenOf, Map<String, Set<String>> parentsOf) {
        Set<String> visited = new HashSet<>();
        List<Set<String>> components = new ArrayList<>();
        for (String group : allGroups) {
            if (visited.contains(group)) {
                continue;
            }
            Set<String> component = new LinkedHashSet<>();
            Deque<String> queue = new ArrayDeque<>(List.of(group));
            while (!queue.isEmpty()) {
                String current = queue.poll();
                if (!component.add(current)) {
                    continue;
                }
                visited.add(current);
                queue.addAll(childrenOf.getOrDefault(current, Set.of()));
                queue.addAll(parentsOf.getOrDefault(current, Set.of()));
            }
            components.add(component);
        }
        return components;
    }

    private static Chain layoutComponent(Set<String> component, Map<String, Set<String>> childrenOf, Map<String, Set<String>> parentsOf) {
        Map<String, Integer> column = assignColumns(component, childrenOf, parentsOf);
        int maxColumn = column.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        List<List<String>> columns = new ArrayList<>();
        for (int i = 0; i <= maxColumn; i++) {
            columns.add(new ArrayList<>());
        }
        for (String group : component) {
            columns.get(column.get(group)).add(group);
        }

        Map<String, Integer> row = new HashMap<>();
        for (List<String> current : columns) {
            current.sort(Comparator.comparingDouble((String group) -> barycenter(group, parentsOf, row)).thenComparing(Comparator.naturalOrder()));
            for (int r = 0; r < current.size(); r++) {
                row.put(current.get(r), r);
            }
        }
        return new Chain(columns);
    }

    /** Longest path (in edges) from any in-component root - guarantees a parent's column is always strictly less than its children's, so left-to-right rendering never has to look ahead. */
    private static Map<String, Integer> assignColumns(Set<String> component, Map<String, Set<String>> childrenOf, Map<String, Set<String>> parentsOf) {
        Map<String, Integer> column = new HashMap<>();
        for (String group : topologicalOrder(component, childrenOf, parentsOf)) {
            int col = 0;
            for (String parent : parentsOf.getOrDefault(group, Set.of())) {
                if (component.contains(parent)) {
                    col = Math.max(col, column.getOrDefault(parent, 0) + 1);
                }
            }
            column.put(group, col);
        }
        return column;
    }

    /** Kahn's algorithm restricted to this component - roots (no in-component parent) first, then every other node once all its in-component parents are placed. */
    private static List<String> topologicalOrder(Set<String> component, Map<String, Set<String>> childrenOf, Map<String, Set<String>> parentsOf) {
        Map<String, Integer> remainingParents = new HashMap<>();
        for (String group : component) {
            int count = 0;
            for (String parent : parentsOf.getOrDefault(group, Set.of())) {
                if (component.contains(parent)) {
                    count++;
                }
            }
            remainingParents.put(group, count);
        }

        List<String> order = new ArrayList<>();
        Deque<String> ready = new ArrayDeque<>();
        remainingParents.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .sorted()
                .forEach(ready::add);

        while (!ready.isEmpty()) {
            String group = ready.poll();
            order.add(group);
            for (String child : childrenOf.getOrDefault(group, Set.of())) {
                if (!component.contains(child)) {
                    continue;
                }
                if (remainingParents.merge(child, -1, Integer::sum) == 0) {
                    ready.add(child);
                }
            }
        }
        return order;
    }

    /** Average row of this group's already-placed in-component parents (0 for a root) - sorting a column by this centers a converging node among its N parents. */
    private static double barycenter(String group, Map<String, Set<String>> parentsOf, Map<String, Integer> row) {
        List<Integer> parentRows = new ArrayList<>();
        for (String parent : parentsOf.getOrDefault(group, Set.of())) {
            Integer parentRow = row.get(parent);
            if (parentRow != null) {
                parentRows.add(parentRow);
            }
        }
        if (parentRows.isEmpty()) {
            return 0;
        }
        return parentRows.stream().mapToInt(Integer::intValue).average().orElse(0);
    }
}
