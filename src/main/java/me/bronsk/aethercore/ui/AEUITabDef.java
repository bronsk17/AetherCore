package me.bronsk.aethercore.ui;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.function.Supplier;

/**
 * One tab: its label, an identity ({@code id}) compared with {@code .equals()} to decide whether
 * it's the active one, and which other identities should still count as "this tab is active" (e.g.
 * a detail view reached only from within this tab's own content, like {@code
 * AEUIPlayerDetailsView} under the Players tab). {@code id} is a {@link Class} for the sidebar's
 * page-level tabs (matched against {@code history.getCurrent().getClass()}), but any
 * equals-comparable value works - a local view can use a plain enum constant instead. {@code
 * content} is only used by {@link me.bronsk.aethercore.ui.components.AEUITabs}'s default
 * page-navigate selection - a caller that supplies its own {@code onSelect} can leave it as a stub.
 * Purely presentational, knows nothing about navigation/history itself. {@code title} is a Lang
 * message code (see {@link me.bronsk.aethercore.ui.framework.AEUIComponent#lang}), resolved to
 * display text by {@code AEUITabs} at render time - never a literal label.
 */
public record AEUITabDef(
        String title,
        Object id,
        Supplier<AEUIComponent> content,
        List<Object> childIds,
        String permission
) {
    public AEUITabDef(String title, Object id, Supplier<AEUIComponent> content) {
        this(title, id, content, List.of(), "");
    }

    public AEUITabDef(String title, Object id, Supplier<AEUIComponent> content, String permission) {
        this(title, id, content, List.of(), permission);
    }
}
