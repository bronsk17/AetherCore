package me.bronsk.aethercore.modules.player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The set of currently-vanished player UUIDs, process-wide - used by the world-map filter predicate
 * every online player's client gets ({@code Player#getWorldMapTracker().setPlayerMapFilter}), which
 * needs to exclude every vanished player at once, not just the one being toggled right now. Kept in
 * sync with {@link me.bronsk.aethercore.modules.player.components.AEPlayerVanishedComponent} by
 * {@code AEGamePlayer#setIsVanished} - this registry is the fast, allocation-free read path for a
 * predicate that runs on every map update, the component is the authoritative per-entity state.
 */
public final class AEVanishRegistry {

    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();

    public static void add(UUID uuid) {
        VANISHED.add(uuid);
    }

    public static void remove(UUID uuid) {
        VANISHED.remove(uuid);
    }

    public static boolean contains(UUID uuid) {
        return VANISHED.contains(uuid);
    }

    private AEVanishRegistry() {
    }
}
