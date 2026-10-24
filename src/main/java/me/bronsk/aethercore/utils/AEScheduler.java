package me.bronsk.aethercore.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * One shared daemon scheduler for delayed, non-tick-critical callbacks (e.g. auto-dismissing a UI
 * notification). Owned by {@code AetherCore}, shut down on plugin shutdown. Callbacks run on this
 * scheduler's own thread, never the world tick thread - anything touching game/UI state must hop
 * back via {@code AEGamePlayer#executeOnWorld}.
 */
public final class AEScheduler {

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "AetherCore-Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private AEScheduler() {
    }

    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return EXECUTOR.schedule(task, delay, unit);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }
}
