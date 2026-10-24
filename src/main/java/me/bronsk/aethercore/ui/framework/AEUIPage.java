package me.bronsk.aethercore.ui.framework;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.utils.AEScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Base for every AetherCore native Custom UI page: owns the render/dispatch lifecycle only. */
public abstract class AEUIPage extends InteractiveCustomUIPage<AEUIEventData> {

    private final Map<String, Consumer<String>> handlers = new HashMap<>();
    private final UUID actorUuid;

    /** Only non-null while a build/refresh/event-batch is in progress - the "currently active" command pair every {@link AEUIRenderer} in that pass writes into. */
    private UICommandBuilder cmd;
    private UIEventBuilder events;

    /** Only non-null if the page opted in via {@link #enableHistory} - history is optional, not every page needs it. */
    private AEUIHistory history;

    /** Only non-null while a popup is open - see {@link #showPopup}. */
    private AEUIComponent popup;

    /** Only non-null while a notification banner is showing - see {@link #showNotification}. */
    private AEActionResult notification;
    private final AtomicLong notificationToken = new AtomicLong();
    private static final long NOTIFICATION_DURATION_SECONDS = 4;

    protected AEUIPage(PlayerRef playerRef, CustomPageLifetime lifetime) {
        super(playerRef, lifetime, AEUIEventData.CODEC);
        this.actorUuid = playerRef.getUuid();
    }

    /** The static shell .ui file, appended once when the page opens. */
    protected abstract String getRootTemplate();

    /** This page's dynamic content, keyed by the shell's named children - recomputed and reapplied on every build/refresh, see {@link AEUIComponent#getContent}. */
    protected abstract Map<String, Object> getContent();

    @Override
    public final void build(Ref<EntityStore> ref, UICommandBuilder cmd, UIEventBuilder events, Store<EntityStore> store) {
        this.cmd = cmd;
        this.events = events;
        cmd.append(getRootTemplate());
        handlers.clear();
        AEUIApplier.refreshAll(new AEUIRenderer("", this), getContent());
        this.cmd = null;
        this.events = null;
    }

    @Override
    public final void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, AEUIEventData data) {
        String action = data.getAction();
        if (action == null) {
            return;
        }
        Consumer<String> handler = handlers.get(action);
        if (handler == null) {
            return;
        }
        runInBatch(() -> handler.accept(data.getValue()));
    }

    /**
     * Rebuilds the whole page from {@link #getContent}. Use this only for structural changes (e.g.
     * switching tabs) - prefer a component's own {@link AEUIComponent#refresh} for localized
     * updates so unrelated elements (like a text field mid-edit) aren't torn down and recreated.
     */
    protected final void refreshAll() {
        runInBatch(() -> {
            handlers.clear();
            AEUIApplier.refreshAll(new AEUIRenderer("", this), getContent());
        });
    }

    /**
     * Opts this page into back/forward history. Call once, from the subclass's constructor, with
     * the initial view. Returns the {@link AEUIHistory} so the subclass can keep its own reference
     * for direct use (nav highlighting, etc.).
     */
    protected final AEUIHistory enableHistory(AEUIComponent initial) {
        AEUIHistory newHistory = new AEUIHistory(initial);
        this.history = newHistory;
        return newHistory;
    }

    protected final void goBack() {
        getHistory().goBack();
        refreshAll();
    }

    protected final void goForward() {
        getHistory().goForward();
        refreshAll();
    }

    public void navigate(AEUIComponent destination) {
        AEUIComponent outgoing = getHistory().getCurrent();
        boolean willReplace = outgoing.getClass() != destination.getClass();
        getHistory().navigate(destination);
        if (willReplace) {
            outgoing.onDetach();
            AEUIRefreshScheduler.unregisterActor(getActorUuid());
        }
        refreshAll();
    }

    /**
     * Shows {@code popup} on top of the rest of this page's content, or hides the current one when
     * {@code popup} is null - requires the shell's root template to declare an "Overlay" slot (see
     * {@code Main.ui}) and {@link #getContent} to map it to {@link #getPopup}. Refreshes only that
     * slot, leaving the rest of the page (a text field mid-edit, scroll position, ...) untouched.
     */
    protected final void showPopup(AEUIComponent popup) {
        this.popup = popup;
        runInBatch(() -> AEUIApplier.refreshKeys(new AEUIRenderer("", this), getContent(), "Overlay"));
    }

    /** Hides the current popup, if any - equivalent to {@code showPopup(null)}. */
    protected final void closePopup() {
        showPopup(null);
    }

    /** The currently shown popup, or null when none is open. */
    protected final AEUIComponent getPopup() {
        return popup;
    }

    /**
     * Shows {@code result} as a colored banner at the top of the page (green on success, red on
     * error) and schedules it to auto-dismiss after {@link #NOTIFICATION_DURATION_SECONDS}. Calling
     * this again before that timer fires replaces the banner outright and supersedes the old timer -
     * see {@link #dismissNotification}. Requires the shell's root template to declare a
     * "Notification" slot (see {@code Main.ui}) and {@link #getContent} to map it to {@link
     * #getNotification}. No-ops when {@code result} or its message is {@code null}, matching the
     * null-tolerance {@code sendToActor} already had.
     */
    protected final void showNotification(AEActionResult result) {
        if (result == null || result.message() == null) {
            return;
        }

        long token = notificationToken.incrementAndGet();
        this.notification = result;
        runInBatch(() -> AEUIApplier.refreshKeys(new AEUIRenderer("", this), getContent(), "Notification"));
        AEScheduler.schedule(() -> dismissNotification(token), NOTIFICATION_DURATION_SECONDS, TimeUnit.SECONDS);
    }

    /** The currently shown notification banner, or null when none is showing. */
    protected final AEActionResult getNotification() {
        return notification;
    }

    /**
     * Runs on {@link AEScheduler}'s own thread, not the world tick thread - hops back via {@link
     * AEGamePlayer#executeOnWorld} before touching any page state. Only clears the banner when
     * {@code token} still matches the most recent {@link #showNotification} call, so a stale timer
     * from a since-replaced notification can't wipe out a newer one.
     */
    private void dismissNotification(long token) {
        AEGamePlayer actor = AEGamePlayer.fromUuid(getActorUuid());
        if (actor == null) {
            return;
        }

        actor.executeOnWorld(() -> {
            if (notificationToken.get() != token) {
                return;
            }
            notification = null;
            runInBatch(() -> AEUIApplier.refreshKeys(new AEUIRenderer("", this), getContent(), "Notification"));
        });
    }

    /** The UUID of the player this page is open for. */
    UUID getActorUuid() {
        return actorUuid;
    }

    private AEUIHistory getHistory() {
        if (history == null) {
            throw new IllegalStateException("This page has no history - call enableHistory(...) first");
        }
        return history;
    }

    /**
     * Runs {@code action} (which should call one or more {@link AEUIComponent#refresh}) against a
     * real command/event batch and sends it - for a UI update NOT triggered synchronously by a
     * client event (e.g. a {@code AEScheduler}-driven periodic auto-refresh). A normal event-handler
     * refresh gets this for free via {@link #handleDataEvent}'s own {@link #runInBatch} wrap and
     * should just call {@link AEUIComponent#refresh} directly instead - calling {@link
     * AEUIComponent#refresh} with no batch open throws (its writes go through {@link #cmd}, which is
     * null outside a batch), and a scheduled callback's exception has nobody to surface it, so the
     * refresh silently does nothing.
     */
    final void runScheduledUpdate(Runnable action) {
        runInBatch(action);
    }

    /**
     * Runs {@code action} against a single shared command/event batch, sending it exactly once
     * when the outermost caller finishes - so a handler that touches several scopes (e.g. two
     * separate {@link AEUIComponent#refresh} calls) still produces one UI update, not one per
     * touched scope. Re-entrant: nested calls (e.g. {@link #refreshAll()} invoked from inside an
     * event handler) join the already-open batch instead of flushing early.
     */
    private void runInBatch(Runnable action) {
        boolean owner = cmd == null;
        if (owner) {
            cmd = new UICommandBuilder();
            events = new UIEventBuilder();
        }
        try {
            action.run();
        } finally {
            if (owner) {
                UICommandBuilder finishedCmd = cmd;
                UIEventBuilder finishedEvents = events;
                cmd = null;
                events = null;
                sendUpdate(finishedCmd, finishedEvents, false);
            }
        }
    }

    UICommandBuilder cmd() {
        return cmd;
    }

    UIEventBuilder events() {
        return events;
    }

    void putHandler(String scope, Consumer<String> handler) {
        handlers.put(scope, handler);
    }

    void purgeHandlersUnder(String scope) {
        handlers.keySet().removeIf(key -> key.equals(scope) || key.startsWith(scope + " ") || key.startsWith(scope + "["));
    }
}
