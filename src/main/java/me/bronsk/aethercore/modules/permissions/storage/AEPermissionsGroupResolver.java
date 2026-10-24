package me.bronsk.aethercore.modules.permissions.storage;

import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

/** Group inheritance DAG resolution shared by every {@link AEPermissionsStorage} backend - a group can inherit from any number of children, so this walks a graph (BFS, visited-set for cycle-safety) rather than a single chain. */
final class AEPermissionsGroupResolver {

    private AEPermissionsGroupResolver() {
    }

    /** Every group reachable from {@code groupName} by following "inherits from" edges, transitively - not including {@code groupName} itself. */
    static Set<String> getTransitiveGroupChildren(AEPermissionsStorage storage, String groupName) throws SQLException {
        Set<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(groupName);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            for (String child : storage.getGroupChildren(current)) {
                if (!visited.contains(child)) {
                    queue.add(child);
                }
            }
        }
        visited.remove(groupName);
        return Collections.unmodifiableSet(visited);
    }

    /** Own permissions plus every reachable child's permissions (transitively), cycle-safe. */
    static Set<String> getEffectiveGroupPermissions(AEPermissionsStorage storage, String groupName) throws SQLException {
        Set<String> visited = new LinkedHashSet<>();
        Set<String> permissions = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(groupName);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            permissions.addAll(storage.getGroupPermissions(current));
            for (String child : storage.getGroupChildren(current)) {
                if (!visited.contains(child)) {
                    queue.add(child);
                }
            }
        }
        return Collections.unmodifiableSet(permissions);
    }
}
