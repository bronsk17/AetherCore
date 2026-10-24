package me.bronsk.aethercore.modules.player.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.sql.AESqlStorage;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.player.models.AEAdminSettings;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.modules.player.models.AEPlayerBan;
import me.bronsk.aethercore.modules.player.models.AEPlayerMute;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AEPlayersSqlStorage extends AESqlStorage implements AEPlayersStorage {

    private final Map<String, AEPlayer> playersByUuid = new ConcurrentHashMap<>();

    /**
     * Creates a new instance.
     *
     * @param connectionSource Method argument.
     * @param dialect Method argument.
     * @throws SQLException when the operation fails.
     */
    public AEPlayersSqlStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws SQLException {
        super(connectionSource, dialect);
    }

    /**
     * Handles createTables.
     *
     * @throws SQLException when the operation fails.
     */
    @Override
    public void createTables() throws SQLException {
        ConnectionSource connectionSource = getConnection();
        TableUtils.createTableIfNotExists(connectionSource, AEPlayer.class);
        TableUtils.createTableIfNotExists(connectionSource, AEPlayerBan.class);
        TableUtils.createTableIfNotExists(connectionSource, AEPlayerMute.class);
        TableUtils.createTableIfNotExists(connectionSource, AEAdminSettings.class);

        ensureColumn(getPlayersDao(), "tcplayers", "lang", "VARCHAR(255)");
        ensureColumn(getPlayersDao(), "tcplayers", AEPlayer.LAST_IP_FIELD, "VARCHAR(45)");

        ensureColumn(getPlayerBansDao(), "tcplayersbans", AEPlayerBan.PLAYER_ID_FIELD, "INT NOT NULL");
        ensureColumn(getPlayerBansDao(), "tcplayersbans", AEPlayerBan.BANNED_BY_FIELD, "INT");
        ensureColumn(getPlayerBansDao(), "tcplayersbans", AEPlayerBan.REVOKED_BY_FIELD, "INT");
        ensureRestrictForeignKey(getPlayerBansDao(), "tcplayersbans", "fk_tcplayersbans_player_id",
                AEPlayerBan.PLAYER_ID_FIELD, "tcplayers", AEPlayer.ID_FIELD);
        ensureRestrictForeignKey(getPlayerBansDao(), "tcplayersbans", "fk_tcplayersbans_banned_by",
                AEPlayerBan.BANNED_BY_FIELD, "tcplayers", AEPlayer.ID_FIELD);
        ensureRestrictForeignKey(getPlayerBansDao(), "tcplayersbans", "fk_tcplayersbans_revoked_by",
                AEPlayerBan.REVOKED_BY_FIELD, "tcplayers", AEPlayer.ID_FIELD);

        ensureColumn(getPlayerMutesDao(), "tcplayersmutes", AEPlayerMute.PLAYER_ID_FIELD, "INT NOT NULL");
        ensureColumn(getPlayerMutesDao(), "tcplayersmutes", AEPlayerMute.MUTED_BY_FIELD, "INT");
        ensureColumn(getPlayerMutesDao(), "tcplayersmutes", AEPlayerMute.REVOKED_BY_FIELD, "INT");
        ensureRestrictForeignKey(getPlayerMutesDao(), "tcplayersmutes", "fk_tcplayersmutes_player_id",
                AEPlayerMute.PLAYER_ID_FIELD, "tcplayers", AEPlayer.ID_FIELD);
        ensureRestrictForeignKey(getPlayerMutesDao(), "tcplayersmutes", "fk_tcplayersmutes_muted_by",
                AEPlayerMute.MUTED_BY_FIELD, "tcplayers", AEPlayer.ID_FIELD);
        ensureRestrictForeignKey(getPlayerMutesDao(), "tcplayersmutes", "fk_tcplayersmutes_revoked_by",
                AEPlayerMute.REVOKED_BY_FIELD, "tcplayers", AEPlayer.ID_FIELD);

        ensureColumn(getAdminSettingsDao(), "aeadminsettings", AEAdminSettings.PLAYER_ID_FIELD, "INT NOT NULL");
        ensureColumn(getAdminSettingsDao(), "aeadminsettings", "fly_speed_level", "INT DEFAULT 1");
        ensureRestrictForeignKey(getAdminSettingsDao(), "aeadminsettings", "fk_aeadminsettings_player_id",
                AEAdminSettings.PLAYER_ID_FIELD, "tcplayers", AEPlayer.ID_FIELD);
    }

    /**
     * Returns getByUuid.
     *
     * @param uuid Method argument.
     * @return Method result.
     * @throws SQLException when the operation fails.
     */
    public AEPlayer getByUuid(String playerUuid) throws SQLException {
        AEPlayer cached = playersByUuid.get(playerUuid);
        if (cached != null) {
            return cached;
        }

        AEPlayer player = getPlayersDao().queryBuilder()
                .where()
                .eq(AEPlayer.UUID_FIELD, playerUuid)
                .queryForFirst();

        if (player != null) {
            playersByUuid.put(playerUuid, player);
        }

        return player;
    }

    /**
     * Returns getByName.
     *
     * @param name Method argument.
     * @return Method result.
     * @throws SQLException when the operation fails.
     */
    public AEPlayer getByName(String name) throws SQLException {
        AEPlayer exact = getPlayersDao().queryBuilder()
                .where()
                .eq(AEPlayer.NAME_FIELD, name)
                .queryForFirst();

        if (exact != null) {
            playersByUuid.put(exact.getUuid(), exact);
            return exact;
        }

        for (AEPlayer player : getPlayersDao().queryForAll()) {
            if (player.getName().equalsIgnoreCase(name)) {
                playersByUuid.put(player.getUuid(), player);
                return player;
            }
        }

        return null;
    }

    /**
     * Creates a new player row from a full {@link AEPlayer} template - id and timestamps are
     * always assigned here, never taken from the input.
     *
     * @param tcPlayer Template with uuid/name/lang/lastIp to persist.
     * @return The newly created, fully-assigned row.
     * @throws SQLException when the operation fails.
     */
    @Override
    public AEPlayer createPlayer(AEPlayer tcPlayer) throws SQLException {
        if (tcPlayer.getUuid() == null) {
            throw new SQLException("Player UUID is null");
        }
        AEPlayer created = new AEPlayer(0, tcPlayer.getUuid(), tcPlayer.getName(), now(), now());
        created.setLang(tcPlayer.getLang());
        created.setLastIp(tcPlayer.getLastIp());
        getPlayersDao().create(created);
        playersByUuid.put(tcPlayer.getUuid(), created);
        return created;
    }

    /**
     * Persists an already-existing player row exactly as given, bumping {@code updatedAt}.
     *
     * @param player The full row to persist.
     * @return The same instance, with {@code updatedAt} refreshed.
     * @throws SQLException when the operation fails.
     */
    @Override
    public AEPlayer updatePlayer(AEPlayer player) throws SQLException {
        player.setUpdatedAt(now());
        getPlayersDao().update(player);
        playersByUuid.put(player.getUuid(), player);
        return player;
    }

    @Override
    public Set<AEPlayer> getPlayers() throws SQLException {
        return new LinkedHashSet<>(getPlayersDao().queryForAll());
    }

    @Override
    public List<AEPlayer> queryPlayers(AEPageQuery query) throws SQLException {
        QueryBuilder<AEPlayer, Integer> builder = getPlayersDao().queryBuilder();
        if (query.hasSearch()) {
            builder.where().like(AEPlayer.NAME_FIELD, "%" + query.search() + "%");
        }
        return builder
                .orderBy(AEPlayer.NAME_FIELD, true)
                .orderBy(AEPlayer.UUID_FIELD, true)
                .offset(query.offset())
                .limit((long) query.pageSize())
                .query();
    }

    @Override
    public List<AEPlayer> getPlayersByLastIp(String ip) throws SQLException {
        return getPlayersDao().queryBuilder()
                .where().eq(AEPlayer.LAST_IP_FIELD, ip)
                .query();
    }

    @Override
    public long countPlayers(AEPageQuery query) throws SQLException {
        if (!query.hasSearch()) {
            return getPlayersDao().countOf();
        }
        QueryBuilder<AEPlayer, Integer> builder = getPlayersDao().queryBuilder();
        builder.where().like(AEPlayer.NAME_FIELD, "%" + query.search() + "%");
        return builder.countOf();
    }

    @Override
    public AEPlayerBan getActiveBan(String playerUuid) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return null;
        }

        AEPlayerBan activeBan = null;
        List<AEPlayerBan> bans = getPlayerBansDao().queryBuilder()
                .where()
                .eq(AEPlayerBan.PLAYER_ID_FIELD, player.getId())
                .and()
                .isNull(AEPlayerBan.REVOKED_AT_FIELD)
                .query();
        for (AEPlayerBan ban : bans) {
            if (!ban.isActive()) {
                continue;
            }
            if (activeBan == null || isAfter(ban.getCreatedAt(), activeBan.getCreatedAt())) {
                activeBan = ban;
            }
        }

        return activeBan;
    }

    @Override
    public List<AEPlayerBan> getBans(String playerUuid) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return List.of();
        }

        List<AEPlayerBan> bans = getPlayerBansDao().queryBuilder()
                .where()
                .eq(AEPlayerBan.PLAYER_ID_FIELD, player.getId())
                .query();

        List<AEPlayerBan> sorted = new ArrayList<>(bans);
        sorted.sort(Comparator.comparing(AEPlayerBan::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return sorted;
    }

    @Override
    public AEPlayerMute getActiveMute(String playerUuid) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return null;
        }

        AEPlayerMute activeMute = null;
        List<AEPlayerMute> mutes = getPlayerMutesDao().queryBuilder()
                .where()
                .eq(AEPlayerMute.PLAYER_ID_FIELD, player.getId())
                .and()
                .isNull(AEPlayerMute.REVOKED_AT_FIELD)
                .query();
        for (AEPlayerMute mute : mutes) {
            if (!mute.isActive()) {
                continue;
            }
            if (activeMute == null || isAfter(mute.getCreatedAt(), activeMute.getCreatedAt())) {
                activeMute = mute;
            }
        }

        return activeMute;
    }

    @Override
    public List<AEPlayerMute> getMutes(String playerUuid) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return List.of();
        }

        List<AEPlayerMute> mutes = getPlayerMutesDao().queryBuilder()
                .where()
                .eq(AEPlayerMute.PLAYER_ID_FIELD, player.getId())
                .query();

        List<AEPlayerMute> sorted = new ArrayList<>(mutes);
        sorted.sort(Comparator.comparing(AEPlayerMute::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return sorted;
    }

    @Override
    public AEPlayerBan createBan(String playerUuid, String bannedByUuid, String reason, Timestamp expiresAt) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return null;
        }

        AEPlayerBan ban = new AEPlayerBan(0, player.getId(), resolvePlayerId(bannedByUuid), reason, expiresAt, now(), now());
        getPlayerBansDao().create(ban);
        return ban;
    }

    @Override
    public AEPlayerMute createMute(String playerUuid, String mutedByUuid, String reason, Timestamp expiresAt) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return null;
        }

        AEPlayerMute mute = new AEPlayerMute(0, player.getId(), resolvePlayerId(mutedByUuid), reason, expiresAt, now(), now());
        getPlayerMutesDao().create(mute);
        return mute;
    }

    @Override
    public boolean revokeBan(String playerUuid, String revokedByUuid, String reason) throws SQLException {
        AEPlayerBan ban = getActiveBan(playerUuid);
        if (ban == null) {
            return false;
        }

        ban.setRevokedAt(now());
        ban.setRevokedBy(resolvePlayerId(revokedByUuid));
        ban.setRevokedReason(reason);
        ban.setUpdatedAt(now());
        getPlayerBansDao().update(ban);
        return true;
    }

    @Override
    public boolean revokeMute(String playerUuid, String revokedByUuid, String reason) throws SQLException {
        AEPlayerMute mute = getActiveMute(playerUuid);
        if (mute == null) {
            return false;
        }

        mute.setRevokedAt(now());
        mute.setRevokedBy(resolvePlayerId(revokedByUuid));
        mute.setRevokedReason(reason);
        mute.setUpdatedAt(now());
        getPlayerMutesDao().update(mute);
        return true;
    }

    @Override
    public AEAdminSettings getOrCreateAdminSettings(String playerUuid) throws SQLException {
        AEPlayer player = getByUuid(playerUuid);
        if (player == null) {
            return null;
        }

        AEAdminSettings existing = getAdminSettingsDao().queryBuilder()
                .where()
                .eq(AEAdminSettings.PLAYER_ID_FIELD, player.getId())
                .queryForFirst();
        if (existing != null) {
            return existing;
        }

        return new AEAdminSettings(0, player.getId(), false, "Adventure", false, false, 1, 1, now(), now());
    }

    @Override
    public AEAdminSettings saveAdminSettings(AEAdminSettings settings) throws SQLException {
        settings.setUpdatedAt(now());
        if (settings.getId() == 0) {
            getAdminSettingsDao().create(settings);
        } else {
            getAdminSettingsDao().update(settings);
        }
        return settings;
    }

    private Dao<AEAdminSettings, Integer> getAdminSettingsDao() throws SQLException {
        return getDao(AEAdminSettings.class);
    }

    /**
     * Handles invalidate.
     *
     * @param uuid Method argument.
     */
    public void invalidate(String playerUuid) {
        playersByUuid.remove(playerUuid);
    }

    /**
     * Returns getPlayersDao.
     *
     * @return Method result.
     * @throws SQLException when the operation fails.
     */
    private Dao<AEPlayer, Integer> getPlayersDao() throws SQLException {
        return getDao(AEPlayer.class);
    }

    private Dao<AEPlayerBan, Integer> getPlayerBansDao() throws SQLException {
        return getDao(AEPlayerBan.class);
    }

    private Dao<AEPlayerMute, Integer> getPlayerMutesDao() throws SQLException {
        return getDao(AEPlayerMute.class);
    }

    private Integer resolvePlayerId(String uuid) throws SQLException {
        if (uuid == null) {
            return null;
        }

        AEPlayer player = getByUuid(uuid);
        return player == null ? null : player.getId();
    }

    @Override
    public AEPlayer getById(int id) throws SQLException {
        return getPlayersDao().queryForId(id);
    }

    private boolean isAfter(Timestamp value, Timestamp other) {
        if (value == null) {
            return false;
        }
        if (other == null) {
            return true;
        }
        return value.after(other);
    }

    /**
     * Clears data through clearCache.
     */
    @Override
    public void clearCache() {
        playersByUuid.clear();
    }

}
