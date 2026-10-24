package me.bronsk.aethercore.ui.framework;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A back/forward history of real, already-built {@link AEUIComponent} instances - no separate
 * "route" value, no resolver: whatever you {@link #navigate} to is exactly what {@link #getCurrent}
 * returns later. Dedup on {@link #navigate} compares by {@code Class}, not identity/equals - two
 * instances of the same view type in a row (e.g. re-clicking the tab you're already on) no-op,
 * while two different types never do. This only stays correct as long as the app's navigation graph
 * never puts two *different* instances of the *same* type back-to-back as current (e.g. jumping
 * straight from one player's details to another's, without passing through the grid in between) -
 * true today because every detail view is only reachable through its own list/grid.
 */
public final class AEUIHistory {

    private AEUIComponent current;
    private final Deque<AEUIComponent> back = new ArrayDeque<>();
    private final Deque<AEUIComponent> forward = new ArrayDeque<>();

    public AEUIHistory(AEUIComponent initial) {
        this.current = initial;
    }

    public AEUIComponent getCurrent() {
        return current;
    }

    /** Navigates to {@code destination}, recording the current one for "back". No-ops if {@code destination} is the same type as the current one. */
    public void navigate(AEUIComponent destination) {
        if (destination.getClass() == current.getClass()) {
            return;
        }
        back.push(current);
        forward.clear();
        current = destination;
    }

    public void goBack() {
        if (back.isEmpty()) {
            return;
        }
        forward.push(current);
        current = back.pop();
    }

    public void goForward() {
        if (forward.isEmpty()) {
            return;
        }
        back.push(current);
        current = forward.pop();
    }
}
