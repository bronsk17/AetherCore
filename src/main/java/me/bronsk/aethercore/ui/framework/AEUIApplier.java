package me.bronsk.aethercore.ui.framework;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Walks a {@link AEUIComponent}'s declared {@link AEUIComponent#getTemplate} / {@link
 * AEUIComponent#getContent} and turns it into real {@link AEUIRenderer} commands - the only place
 * in the framework that ever calls {@code appendChild}/{@code set}/{@code onActivate} on a
 * component's behalf.
 */
final class AEUIApplier {

    private AEUIApplier() {
    }

    /** Applies {@code component} into {@code scope} - appended fresh at {@code index} if it owns a template, bound directly onto {@code scope} otherwise. */
    static void apply(AEUIRenderer scope, AEUIComponent component, int index) {
        String template = component.getTemplate();
        AEUIRenderer target = template != null ? scope.appendChild(template, index) : scope;
        component.bind(target);
        applyContent(target, component.getContent());
    }

    /** Applies every entry of {@code content} directly onto the already-resolved {@code target} - no clearing, only safe for freshly-created scopes. */
    static void applyContent(AEUIRenderer target, Map<String, Object> content) {
        content.forEach((key, value) -> applyEntry(target, key, value));
    }

    /** Re-applies only {@code keys} of {@code component}'s current content, tearing down and rebuilding just those child scopes - every other key is left untouched. */
    static void refreshKeys(AEUIComponent component, String... keys) {
        refreshKeys(component.getBoundScope(), component.getContent(), keys);
    }

    /** Same as {@link #refreshKeys(AEUIComponent, String...)}, for a page rather than a component - see {@link AEUIPage#showPopup}. */
    static void refreshKeys(AEUIRenderer target, Map<String, Object> content, String... keys) {
        for (String key : keys) {
            refreshEntry(target, key, content.get(key));
        }
    }

    /** Re-applies every entry of {@code content} onto {@code target} - a full rebuild, used for page-level (route) refreshes. */
    static void refreshAll(AEUIRenderer target, Map<String, Object> content) {
        content.forEach((key, value) -> refreshEntry(target, key, value));
    }

    private static void applyEntry(AEUIRenderer target, String key, Object value) {
        switch (value) {
            case Runnable r -> target.onActivate(r);
            case Consumer<?> c -> target.onValueChanged(asStringConsumer(c));
            case AEUIComponent child -> apply(scopeFor(target, key), child, 0);
            case List<?> list when isComponentList(list) -> appendAll(scopeFor(target, key), list);
            case List<?> list -> target.set(key, list);
            case Boolean b -> target.set(key, b.booleanValue());
            case Float f -> target.set(key, f.floatValue());
            default -> target.set(key, value);
        }
    }

    private static void refreshEntry(AEUIRenderer target, String key, Object value) {
        switch (value) {
            case Runnable r -> target.onActivate(r);
            case Consumer<?> c -> target.onValueChanged(asStringConsumer(c));
            case AEUIComponent child -> scopeFor(target, key).refreshSelf(s -> apply(s, child, 0));
            case List<?> list when isComponentList(list) -> scopeFor(target, key).refreshSelf(s -> appendAll(s, list));
            case List<?> list -> target.set(key, list);
            case Boolean b -> target.set(key, b.booleanValue());
            case Float f -> target.set(key, f.floatValue());
            default -> target.set(key, value);
        }
    }

    private static void appendAll(AEUIRenderer scope, List<?> items) {
        int index = 0;
        for (Object item : items) {
            apply(scope, (AEUIComponent) item, index++);
        }
    }

    /** {@code ""} means "myself, no descent"; a dotted key like {@code "Avatar.Initial"} descends through several statically-declared child ids in one go. */
    private static AEUIRenderer scopeFor(AEUIRenderer target, String key) {
        if (key.isEmpty()) {
            return target;
        }
        AEUIRenderer scope = target;
        for (String segment : key.split("\\.")) {
            scope = scope.child(segment);
        }
        return scope;
    }

    /** A component list (append each as a child) vs. a plain typed-list attribute (e.g. a dropdown's Entries) - distinguished by element type, empty defaults to "components". */
    private static boolean isComponentList(List<?> list) {
        return list.isEmpty() || list.get(0) instanceof AEUIComponent;
    }

    @SuppressWarnings("unchecked")
    private static Consumer<String> asStringConsumer(Consumer<?> c) {
        return (Consumer<String>) c;
    }
}
