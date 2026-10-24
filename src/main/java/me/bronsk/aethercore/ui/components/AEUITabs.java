package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;

import java.util.List;
import java.util.Map;

/**
 * A tab button list that also tracks which tab is current and resolves its content - construct it
 * once, keep it as a field, and call {@link #current()} wherever the selected tab's content is
 * needed instead of re-deriving "which tab is active" by hand at every call site.
 *
 * <p>Two shapes: the sidebar's page-level tabs (2-arg constructor - vertical, clicking navigates the
 * whole page via the tab's own {@link AEUITabDef#content()} supplier, so {@code currentId} is a
 * {@code Class} matched against {@code history.getCurrent().getClass()} - a fresh instance rebuilt
 * every render is fine here since {@code navigate()} triggers a full page rebuild anyway) and a
 * view's own local sub-tabs (4-arg constructor - horizontal, clicking only flips this instance's own
 * {@code currentId} and runs {@code onChange} so the owner can refresh itself; keep *this* instance
 * as a field so the selection actually persists across renders).
 */
public final class AEUITabs extends AEUIComponent {

    private final List<AEUITabDef> tabs;
    private final boolean vertical;
    private final Runnable onChange;
    private Object currentId;

    public AEUITabs(List<AEUITabDef> tabs, Object currentId) {
        this(tabs, currentId, true, null);
    }

    public AEUITabs(List<AEUITabDef> tabs, Object currentId, boolean vertical, Runnable onChange) {
        this.tabs = tabs;
        this.currentId = currentId;
        this.vertical = vertical;
        this.onChange = onChange;
    }

    /** The currently active tab's own content, resolved via its {@link AEUITabDef#content()} supplier. */
    public AEUIComponent current() {
        return resolveCurrent().content().get();
    }

    @Override
    protected Map<String, Object> getContent() {
        List<AEUIComponent> buttons = tabs.stream()
                .map(tab -> (AEUIComponent) new AETabButton(lang(tab.title()), isActive(tab), () -> select(tab), vertical))
                .toList();
        return Map.of("LayoutMode", vertical ? "Top" : "CenterMiddle", "", buttons);
    }

    private boolean isActive(AEUITabDef tab) {
        return tab.id().equals(currentId) || tab.childIds().contains(currentId);
    }

    private AEUITabDef resolveCurrent() {
        return tabs.stream()
                .filter(tab -> tab.id().equals(currentId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No tab with id " + currentId));
    }

    private void select(AEUITabDef tab) {
        currentId = tab.id();
        if (onChange != null) {
            onChange.run();
        } else {
            navigate(tab.content().get());
        }
    }
}
