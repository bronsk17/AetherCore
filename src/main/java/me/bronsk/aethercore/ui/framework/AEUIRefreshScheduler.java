package me.bronsk.aethercore.ui.framework;

import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.utils.AEScheduler;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central registry backing {@link AEUIComponent#autoRefresh}. Subscriptions are bucketed by their
 * {@link Duration} - every component asking for the same interval shares ONE self-rescheduling
 * {@link AEScheduler} chain instead of each owning its own, so N components auto-refreshing at 5s
 * cost one repeating tick, not N (previously every auto-refreshing view, e.g. the Overview tab,
 * hand-rolled its own independent chain plus its own "am I still the active one" bookkeeping).
 *
 * <p>Only one subscription stays active per admin (UUID) at a time - registering again, even from a
 * different component instance or class (e.g. navigating from one player's Overview to another's),
 * supersedes whatever that admin had running before. A superseded subscription discovers this on
 * its own next tick and drops itself. {@link AEUIPage#navigate} additionally calls
 * {@link #unregisterActor} whenever it replaces the visible component with a different one, so a
 * pending tick never fires a keyed {@link AEUIComponent#refresh} against selectors that belonged to
 * the component that's no longer on screen - the native client treats an unresolved selector as
 * fatal and disconnects the player.
 */
final class AEUIRefreshScheduler {

    private static final Map<UUID, Long> ACTIVE_GENERATION = new ConcurrentHashMap<>();
    private static final AtomicLong GENERATION_SEQUENCE = new AtomicLong();
    private static final Map<Duration, Bucket> BUCKETS = new ConcurrentHashMap<>();

    static void register(AEUIComponent component, UUID actorUuid, Duration interval, String[] keys) {
        long generation = GENERATION_SEQUENCE.incrementAndGet();
        ACTIVE_GENERATION.put(actorUuid, generation);
        Subscription subscription = new Subscription(component, actorUuid, keys, generation);
        BUCKETS.computeIfAbsent(interval, Bucket::new).add(subscription);
    }

    /**
     * Drops whatever subscription {@code actorUuid} currently has, if any - called whenever
     * {@link AEUIPage#navigate} replaces the visible component with a different one, since a
     * pending tick's {@code keys} target selectors that belong to the OUTGOING component's template
     * and may not exist at all on the new one (the native client disconnects the player on a
     * "Selected element ... was not found" CustomUI command otherwise). Safe to call even when
     * nothing is registered for this actor.
     */
    static void unregisterActor(UUID actorUuid) {
        ACTIVE_GENERATION.remove(actorUuid);
    }

    private record Subscription(AEUIComponent component, UUID actorUuid, String[] keys, long generation) {
        boolean isCurrent() {
            return Long.valueOf(generation).equals(ACTIVE_GENERATION.get(actorUuid));
        }
    }

    /**
     * One shared, self-rescheduling tick per distinct interval. Keeps rescheduling itself forever
     * once created, even with zero live subscribers - the alternative (removing an empty bucket from
     * {@link #BUCKETS}) races against a concurrent {@link #register} for that same interval picking
     * this bucket via {@code computeIfAbsent} right as it's being torn down. An idle bucket doing
     * nothing every few seconds is free; that race is not worth avoiding it.
     */
    private static final class Bucket {
        private final long intervalMillis;
        private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

        Bucket(Duration interval) {
            this.intervalMillis = interval.toMillis();
            scheduleTick();
        }

        void add(Subscription subscription) {
            subscriptions.add(subscription);
        }

        private void scheduleTick() {
            AEScheduler.schedule(this::tick, intervalMillis, TimeUnit.MILLISECONDS);
        }

        /** Runs on {@link AEScheduler}'s own thread - hops to each subscriber's own world thread before touching UI state, same as every other {@code AEScheduler} callback in this codebase. */
        private void tick() {
            for (Subscription subscription : subscriptions) {
                if (!subscription.isCurrent()) {
                    subscriptions.remove(subscription);
                    continue;
                }
                AEGamePlayer actor = AEGamePlayer.fromUuid(subscription.actorUuid());
                if (actor == null) {
                    subscriptions.remove(subscription);
                    continue;
                }
                actor.executeOnWorld(() -> subscription.component().runScheduledUpdate(() -> {
                    if (subscription.keys() == null) {
                        subscription.component().refreshAllContent();
                    } else {
                        subscription.component().refresh(subscription.keys());
                    }
                }));
            }
            scheduleTick();
        }
    }

    private AEUIRefreshScheduler() {
    }
}
