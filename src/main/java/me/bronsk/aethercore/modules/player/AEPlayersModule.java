package me.bronsk.aethercore.modules.player;

import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.config.AEModuleConfig;
import me.bronsk.aethercore.events.listeners.AEBreakBlockEventSystem;
import me.bronsk.aethercore.events.listeners.AEDamageBlockEventSystem;
import me.bronsk.aethercore.events.listeners.AEPlaceBlockEventSystem;
import me.bronsk.aethercore.events.listeners.AEPlayerEvents;
import me.bronsk.aethercore.events.listeners.AEUseBlockEventSystem;
import me.bronsk.aethercore.modules.AEModule;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.admintools.AEAdminToolsModule;
import me.bronsk.aethercore.modules.player.models.AEModerationHistoryQuery;
import me.bronsk.aethercore.modules.player.models.AEModerationHistoryResponse;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.modules.player.models.AEPlayerBan;
import me.bronsk.aethercore.modules.player.models.AEPlayerModerationHistoryEntry;
import me.bronsk.aethercore.modules.player.models.AEPlayerMute;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.modules.player.storage.AEPlayersSqlStorage;
import me.bronsk.aethercore.modules.player.storage.AEPlayersStorage;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class AEPlayersModule extends AEModule<AEModuleConfig, AEPlayersStorage> {

    public static final String NAME = "players";

    /**
     * Creates the player module.
     */
    public AEPlayersModule() {
        addSubModule(new AEPlayersModerationModule());
        addSubModule(new AEAdminToolsModule());
    }

    public AEAdminToolsModule getAdminToolsModule() {
        return getSubModule(AEAdminToolsModule.class);
    }

    /**
     * Registers the player listener after storage initialization.
     */
    @Override
    protected void onInit() {
        AetherPlugin plugin = AetherPlugin.getPlugin();
        new AEPlayerEvents(plugin.getEventRegistry());
        plugin.getEntityStoreRegistry().registerSystem(new AEBreakBlockEventSystem());
        plugin.getEntityStoreRegistry().registerSystem(new AEDamageBlockEventSystem());
        plugin.getEntityStoreRegistry().registerSystem(new AEPlaceBlockEventSystem());
        plugin.getEntityStoreRegistry().registerSystem(new AEUseBlockEventSystem());
        registerListener(new AEPlayerListener(this));
    }

    @Override
    protected List<Class<? extends AEModule<?, ?>>> getRequiredModules() {
        return List.of();
    }

    @Override
    protected AEPlayersStorage createStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws Exception {
        return new AEPlayersSqlStorage(connectionSource, dialect);
    }

    /**
     * Returns the stable module name.
     *
     * @return Module name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Handles a player join by creating the database row when it does not exist.
     *
     * @param gamePlayer Joined player.
     */
    public void handleJoin(AEGamePlayer gamePlayer) {
        try {
            UUID uuid = gamePlayer.getUuid();
            String name = gamePlayer.getUsername();
            String ip = gamePlayer.getIp();

            AEPlayer existing = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getByUuid(uuid.toString()));
            if (existing != null) {
                existing.setName(name);
                existing.setLastIp(ip);
                AEDatabaseUtils.runUnchecked(() -> getStorage().updatePlayer(existing));
            } else {
                AEPlayer newPlayer = new AEPlayer(uuid.toString(), name);
                newPlayer.setLastIp(ip);
                AEDatabaseUtils.runUnchecked(() -> getStorage().createPlayer(newPlayer));
            }

            if (getModulesManager().hasModule(AEPermissionsModule.class)) {
                AEPermissionsModule permissionsModule = getModulesManager().getModule(AEPermissionsModule.class);
                if (permissionsModule.isInitialized()) {
                    permissionsModule.getUsersModule().assignDefaultGroupIfNeeded(gamePlayer.getUuid());
                }
            }

            getAdminToolsModule().applyOnJoin(uuid);
        } catch (Exception ex) {
            AELogger.logException(ex);
        }
    }

    /**
     * Resolves a player identifier by UUID first, then by username.
     *
     * @param input UUID or username.
     * @return Resolved UUID, or null when the player cannot be resolved.
     */
    public UUID resolvePlayerUuid(String input) {
        UUID uuid = parseUuid(input);
        if (uuid != null && hasKnownUuid(uuid)) {
            return uuid;
        }
        PlayerRef onlinePlayer = Universe.get().getPlayerByUsername(input, NameMatching.EXACT_IGNORE_CASE);
        if (onlinePlayer != null) {
            return onlinePlayer.getUuid();
        }
        AEPlayer storedPlayer = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getByName(input));
        if (storedPlayer == null) {
            return null;
        }
        return parseUuid(storedPlayer.getUuid());
    }

    /**
     * Returns the latest stored player name for a known UUID.
     *
     * @param uuid Player UUID.
     * @return Stored player name, or null when the player is unknown.
     */
    public String getPlayerName(UUID uuid) {
        AEPlayer player = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getByUuid(uuid.toString()));
        if (player == null) {
            return null;
        }

        return player.getName();
    }

    /**
     * Checks whether a UUID belongs to an online player or a stored player row.
     *
     * @param uuid Player UUID.
     * @return True when the player is known.
     */
    private boolean hasKnownUuid(UUID uuid) {
        if (Universe.get().getPlayer(uuid) != null) {
            return true;
        }

        AEPlayer storedPlayer = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getByUuid(uuid.toString()));
        return storedPlayer != null;
    }

    /**
     * Parses a UUID without throwing for normal command input.
     *
     * @param input Input text.
     * @return Parsed UUID, or null when input is not a UUID.
     */
    private UUID parseUuid(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public AEPlayer getPlayerByUuid(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getByUuid(uuid.toString()));
    }

    /**
     * Returns one page of a player's moderation history (bans + mutes merged, most recent first),
     * with search and pagination applied server-side.
     *
     * @param query History page request.
     * @return Immutable page result.
     */
    public AEModerationHistoryResponse getModerationHistory(AEModerationHistoryQuery query) {
        List<AEPlayerModerationHistoryEntry> entries = AEDatabaseUtils.supplyUnchecked(() -> getModerationHistoryEntries(query.playerUuid()));

        List<AEPlayerModerationHistoryEntry> filtered = query.hasSearch()
                ? entries.stream().filter(entry -> matchesSearch(entry, query.search())).toList()
                : entries;

        long offset = query.offset();
        List<AEPlayerModerationHistoryEntry> paged = filtered.stream()
                .skip(offset)
                .limit(query.pageSize())
                .toList();

        return new AEModerationHistoryResponse(query, paged, filtered.size());
    }

    private List<AEPlayerModerationHistoryEntry> getModerationHistoryEntries(String playerUuid) throws SQLException {
        List<AEPlayerModerationHistoryEntry> entries = new ArrayList<>();
        for (AEPlayerBan ban : getStorage().getBans(playerUuid)) {
            entries.add(new AEPlayerModerationHistoryEntry("Ban", getModerationStatus(ban.getRevokedAt(), ban.getExpiresAt()),
                    ban.getReason(), getPlayerName(ban.getBannedBy()), getPlayerName(ban.getRevokedBy()),
                    ban.getCreatedAt(), ban.getExpiresAt()));
        }
        for (AEPlayerMute mute : getStorage().getMutes(playerUuid)) {
            entries.add(new AEPlayerModerationHistoryEntry("Mute", getModerationStatus(mute.getRevokedAt(), mute.getExpiresAt()),
                    mute.getReason(), getPlayerName(mute.getMutedBy()), getPlayerName(mute.getRevokedBy()),
                    mute.getCreatedAt(), mute.getExpiresAt()));
        }
        entries.sort(Comparator.comparing(AEPlayerModerationHistoryEntry::createdAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return entries;
    }

    private static String getModerationStatus(Timestamp revokedAt, Timestamp expiresAt) {
        if (revokedAt != null) {
            return "Revoked";
        }
        if (expiresAt != null && expiresAt.before(new Timestamp(System.currentTimeMillis()))) {
            return "Expired";
        }
        return "Active";
    }

    private static boolean matchesSearch(AEPlayerModerationHistoryEntry entry, String search) {
        String needle = search.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(entry.type(), needle)
                || containsIgnoreCase(entry.status(), needle)
                || containsIgnoreCase(entry.reason(), needle)
                || containsIgnoreCase(entry.by(), needle)
                || containsIgnoreCase(entry.revokedBy(), needle);
    }

    private static boolean containsIgnoreCase(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    /**
     * Returns the stored player name for an internal storage row id, falling back to "-" when
     * there is no actor (id is null) or the actor is unknown.
     *
     * @param id Internal player row id, or null when there is no actor.
     * @return Display name, or "-".
     */
    private String getPlayerName(Integer id) {
        if (id == null) {
            return "-";
        }
        AEPlayer player = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getById(id));
        return player == null ? "-" : player.getName();
    }

    public Set<AEPlayer> getPlayers() {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayers());
    }

    /**
     * Returns one page of every player registered in this module's storage.
     *
     * @param query Page request.
     * @return Immutable page result.
     */
    public AEPageResult<AEPlayer> getPlayerQueryResult(AEPageQuery query) {
        return AEDatabaseUtils.supplyUnchecked(() -> new AEPageResult<>(
                query,
                getStorage().queryPlayers(query),
                getStorage().countPlayers(query)));
    }

    /** Every player whose last recorded connection IP matches {@code ip} - used by the Dupe IP lookup. */
    public List<AEPlayer> getPlayersByIp(String ip) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPlayersByLastIp(ip));
    }

    public AEPlayersModerationModule getModerationModule() {
        return getSubModule(AEPlayersModerationModule.class);
    }
}
