package me.bronsk.aethercore.ui.framework;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import me.bronsk.aethercore.modules.AEActionResult;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Points at one element's selector ("scope") within a {@link AEUIPage}'s tree and writes into
 * whatever command batch that page currently has open (initial build, a full refresh, or a single
 * event dispatch - see {@link AEUIPage}).
 *
 * <p>{@link #child(String)} descends into a statically-declared nested element; {@link
 * #appendChild(String, int)} appends a fresh instance from a template and returns it scoped to
 * that instance's own root - callers must never re-mention that root's own id afterward.
 */
public final class AEUIRenderer {

    private final String scope;
    private final AEUIPage page;

    AEUIRenderer(String scope, AEUIPage page) {
        this.scope = scope;
        this.page = page;
    }

    /** Descends into a statically-declared child element by id within the current scope. */
    public AEUIRenderer child(String id) {
        return new AEUIRenderer(scope.isEmpty() ? "#" + id : scope + " #" + id, page);
    }

    /** Appends a new instance of {@code template} as the (index)-th child of this scope. */
    public AEUIRenderer appendChild(String template, int index) {
        page.cmd().append(scope, template);
        return new AEUIRenderer(scope + "[" + index + "]", page);
    }

    /** Removes every existing child of this scope. */
    public AEUIRenderer clear() {
        page.cmd().clear(scope);
        return this;
    }

    /** Sets a property directly on this scope's own element. */
    public AEUIRenderer set(String property, Object value) {
        page.cmd().set(scope + "." + property, String.valueOf(value));
        return this;
    }

    /**
     * Sets a boolean property directly on this scope's own element, using the SDK's boolean-typed
     * {@code UICommandBuilder.set(String, boolean)} overload so the wire payload is a real BSON
     * boolean (e.g. {@code {"0":true}}) instead of the quoted-string BSON the generic {@link
     * #set(String, Object)} would produce (e.g. {@code {"0":"true"}}) - some native elements (e.g.
     * {@code CheckBox.Value}) reject the wrong BSON type outright rather than coercing it.
     */
    public AEUIRenderer set(String property, boolean value) {
        page.cmd().set(scope + "." + property, value);
        return this;
    }

    /**
     * Sets a float property directly on this scope's own element, using the SDK's float-typed
     * {@code UICommandBuilder.set(String, float)} overload - same reasoning as the boolean overload
     * above ({@code ProgressBar.Value} is a real BSON float, not a string).
     */
    public AEUIRenderer set(String property, float value) {
        page.cmd().set(scope + "." + property, value);
        return this;
    }

    /** Sets a typed list property (e.g. DropdownBox.Entries) directly on this scope's own element. */
    public <T> AEUIRenderer set(String property, List<T> value) {
        page.cmd().set(scope + "." + property, value);
        return this;
    }

    /**
     * Binds an Activating (click) event on this scope's own element to {@code handler}. The
     * handler runs exactly where it was registered - no action string, no parent forwarding.
     */
    public AEUIRenderer onActivate(Runnable handler) {
        page.putHandler(scope, ignored -> handler.run());
        page.events().addEventBinding(CustomUIEventBindingType.Activating, scope,
                EventData.of(AEUIEventData.KEY_ACTION, scope), false);
        return this;
    }

    /** Binds a ValueChanged event on this scope's own element, passing its live .Value. */
    public AEUIRenderer onValueChanged(Consumer<String> handler) {
        page.putHandler(scope, handler);
        page.events().addEventBinding(CustomUIEventBindingType.ValueChanged, scope,
                EventData.of(AEUIEventData.KEY_ACTION, scope).append(AEUIEventData.KEY_VALUE, scope + ".Value"), false);
        return this;
    }

    /**
     * Re-renders just this element's own subtree in place: clears it and re-runs {@code body}
     * against it. Everything outside this scope - siblings, ancestors, a text field mid-edit
     * elsewhere on the page - is left completely untouched. Safe to call multiple times within the
     * same event handler; all of them land in one combined UI update (see {@link AEUIPage}).
     */
    public void refreshSelf(Consumer<AEUIRenderer> body) {
        page.purgeHandlersUnder(scope);
        body.accept(this.clear());
    }

    /** Navigates the owning page to {@code destination} - see {@link AEUIPage#enableHistory}. */
    void navigate(AEUIComponent destination) {
        page.navigate(destination);
    }

    /** Goes back to the previously shown view - see {@link AEUIPage#enableHistory}. */
    void goBack() {
        page.goBack();
    }

    /** Re-does a navigation previously undone by {@link #goBack} - see {@link AEUIPage#enableHistory}. */
    void goForward() {
        page.goForward();
    }

    /** Rebuilds the whole owning page in place - see {@link AEUIComponent#refreshCurrentView}. */
    void refreshCurrentView() {
        page.refreshAll();
    }

    /** Shows {@code popup} on top of the owning page - see {@link AEUIPage#showPopup}. */
    void showPopup(AEUIComponent popup) {
        page.showPopup(popup);
    }

    /** Hides the current popup, if any - see {@link AEUIPage#closePopup}. */
    void closePopup() {
        page.closePopup();
    }

    /** Shows a notification banner on the owning page - see {@link AEUIPage#showNotification}. */
    void showNotification(AEActionResult result) {
        page.showNotification(result);
    }

    /** The UUID of the player the owning page is open for. */
    UUID getActorUuid() {
        return page.getActorUuid();
    }

    /** Runs a non-event-triggered UI update on the owning page - see {@link AEUIPage#runScheduledUpdate}. */
    void runScheduledUpdate(Runnable action) {
        page.runScheduledUpdate(action);
    }
}
