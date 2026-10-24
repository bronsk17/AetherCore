package me.bronsk.aethercore.ui.framework;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEExecutionContextFactory;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.player.AEGamePlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Base for anything that isn't a page. A component only *describes* itself - it never touches a
 * {@link AEUIRenderer} directly. {@link AEUIApplier} is the sole place that turns a description
 * into real commands (append/set/events), which is what makes {@link #refresh} generic instead of
 * something every component has to hand-implement.
 */
public abstract class AEUIComponent {

    private AEUIRenderer boundScope;
    private boolean autoRefreshArmed;

    /**
     * The {@code .ui} file this component appends as a fresh instance of itself, or {@code null}
     * to bind directly onto whatever scope it's given instead (e.g. a value living on a child
     * element the parent already declared in its own template - {@code AELabel} works this way).
     */
    protected String getTemplate() {
        return null;
    }

    /**
     * What belongs on this component's own scope. The value's type decides what happens with it:
     * <ul>
     *   <li>A {@link AEUIComponent} or {@code List<AEUIComponent>} value DOES descend - the key is
     *   a child id (or dotted path through several statically-declared child ids, e.g.
     *   {@code "Avatar.Initial"}) naming where that child/those children get applied.</li>
     *   <li>A plain attribute value, a {@code Runnable} (Activating) or a {@code Consumer<String>}
     *   (ValueChanged) is bound directly on THIS component's own scope, no matter what the key
     *   string is - it's just a label, not a selector. To bind a click/value handler on a
     *   different, nested element, wrap it in its own small {@link AEUIComponent} (own template,
     *   binds "" on itself - see {@code AEActionButton}) and supply THAT as a child value instead.
     *   {@code ""} as the key always means "myself, no descent" for the descending cases above.</li>
     * </ul>
     */
    protected Map<String, Object> getContent() {
        return Map.of();
    }

    /**
     * Re-applies only {@code keys} of {@link #getContent}, tearing down and rebuilding just those
     * child scopes - every other key already on screen is left completely untouched. Call this
     * after mutating whatever state {@link #getContent} reads.
     */
    protected final void refresh(String... keys) {
        AEUIApplier.refreshKeys(this, keys);
    }

    /**
     * Called on the outgoing component right before {@link AEUIPage#navigate} replaces it with a
     * different one - the hook for stopping any background work this component started (e.g. a
     * {@code AEScheduler}-driven periodic auto-refresh loop), so it doesn't keep touching a selector
     * that no longer exists once the page has moved on to different content (which the native client
     * treats as a fatal error - "Selected element in CustomUI command was not found" disconnects the
     * player). Default no-op. Not called on a same-type {@code navigate()} no-op (see {@link
     * AEUIHistory}), nor on {@code goBack()}/{@code goForward()} - nothing is actually replaced
     * there, the outgoing component just moves further down history and stays reachable.
     */
    protected void onDetach() {
    }

    /**
     * Navigates the page this component lives in to {@code destination}, however deeply nested
     * this component is - no need to thread a navigation callback through every constructor in
     * between. Only valid on a page that called {@link AEUIPage#enableHistory}.
     */
    protected final void navigate(AEUIComponent destination) {
        boundScope.navigate(destination);
    }

    /** Goes back to the previously shown view - see {@link AEUIPage#enableHistory}. */
    protected final void goBack() {
        boundScope.goBack();
    }

    /** Re-does a navigation previously undone by {@link #goBack} - see {@link AEUIPage#enableHistory}. */
    protected final void goForward() {
        boundScope.goForward();
    }

    /** Shows {@code popup} on top of the rest of the page, however deeply nested this component is. */
    protected final void showPopup(AEUIComponent popup) {
        boundScope.showPopup(popup);
    }

    /** Hides the current popup, if any. */
    protected final void closePopup() {
        boundScope.closePopup();
    }

    /** Shows {@code result} as a colored banner at the top of the page - see {@link AEUIPage#showNotification}. */
    protected final void showNotification(AEActionResult result) {
        boundScope.showNotification(result);
    }

    /** The UUID of the player this component's page is open for - the "admin" running a UI action. */
    protected final UUID getActorUuid() {
        return boundScope.getActorUuid();
    }

    /**
     * Runs {@code action} (which should call {@link #refresh} one or more times) against a real
     * network update batch - required for any refresh NOT triggered synchronously by a client event,
     * e.g. a periodic {@code AEScheduler}-driven auto-refresh. An event-handler-triggered refresh
     * (a button click, a value change) gets this for free already - call {@link #refresh} directly
     * there instead; wrapping it in this too would be harmless but redundant.
     */
    protected final void runScheduledUpdate(Runnable action) {
        boundScope.runScheduledUpdate(action);
    }

    /**
     * Registers this component to have its entire content ({@link #getContent}'s keys) refreshed
     * every {@code interval} - see {@link #autoRefresh(Duration, String...)} for refreshing only
     * specific keys instead. Safe to call unconditionally from {@link #getContent} every time it
     * runs - only the very first call on a given instance actually arms anything, later ones are
     * no-ops. Only one auto-refresh loop stays active per admin (this component's {@link
     * #getActorUuid()}) at a time: registering again - even from a different component instance or
     * class, e.g. navigating from one player's Overview to another's - supersedes whatever that
     * admin had running before, so nothing needs to be explicitly stopped when navigating away. See
     * {@link AEUIRefreshScheduler}.
     */
    protected final void autoRefresh(Duration interval) {
        autoRefresh(interval, (String[]) null);
    }

    /** Same as {@link #autoRefresh(Duration)}, but each tick only refreshes {@code keys} (via {@link #refresh}) instead of this component's entire content. */
    protected final void autoRefresh(Duration interval, String... keys) {
        if (autoRefreshArmed) {
            return;
        }
        autoRefreshArmed = true;
        AEUIRefreshScheduler.register(this, getActorUuid(), interval, keys);
    }

    /** Refreshes every key this component currently declares - what a null-keys {@link #autoRefresh} tick calls. */
    final void refreshAllContent() {
        refresh(getContent().keySet().toArray(new String[0]));
    }

    /**
     * Rebuilds the whole page this component lives in, in place - re-runs every visible component's
     * {@link #getContent} from scratch, including any nested table/query that fetches its own data
     * fresh on every render. Does not construct new Java instances for the current view itself (a
     * detail view's own cached field, e.g. the group/player it was opened for, stays as-is) - see the
     * refresh button next to the history nav arrows.
     */
    protected final void refreshCurrentView() {
        boundScope.refreshCurrentView();
    }

    /** An execution context for the admin running this UI action - for module handler calls that need one. */
    protected final AEExecutionContext getActorContext() {
        return AEExecutionContextFactory.get().fromUuid(getActorUuid());
    }

    /**
     * Resolves a Lang-system message code to plain text in the actor's configured language - use
     * this for every user-visible UI label (titles, buttons, headers, placeholders, ...) instead of
     * a hardcoded literal, so it goes through the same {@code /aether lang} system as every other
     * player-facing message and can be retranslated without a rebuild.
     */
    protected final String lang(String code) {
        return getLangModule().buildText(code, getActorContext());
    }

    /** Same as {@link #lang(String)}, substituting {@code args} (see {@link AELangModule#args}) into the resolved text. */
    protected final String lang(String code, Map<String, String> args) {
        return getLangModule().buildText(code, getActorContext(), args);
    }

    private static AELangModule getLangModule() {
        return AEModulesManager.get().getModule(AELangModule.class);
    }

    /** Sends a module handler's result (e.g. from a Ban/Save action) back to the admin, if they're online. */
    protected final void sendToActor(Message message) {
        if (message == null) {
            return;
        }
        AEGamePlayer actor = AEGamePlayer.fromUuid(getActorUuid());
        if (actor != null) {
            actor.sendMessage(message);
        }
    }

    /** Set by {@link AEUIApplier} right after resolving this component's real scope - never call this directly. */
    final void bind(AEUIRenderer scope) {
        this.boundScope = scope;
    }

    final AEUIRenderer getBoundScope() {
        return boundScope;
    }
}
