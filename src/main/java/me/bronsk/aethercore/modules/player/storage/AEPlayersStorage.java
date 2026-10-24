package me.bronsk.aethercore.modules.player.storage;

import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.player.models.AEAdminSettings;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.modules.player.models.AEPlayerBan;
import me.bronsk.aethercore.modules.player.models.AEPlayerMute;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

public interface AEPlayersStorage extends AutoCloseable {

    AEPlayer getByUuid(String playerUuid) throws SQLException;

    AEPlayer getByName(String name) throws SQLException;

    AEPlayer getById(int id) throws SQLException;

    AEPlayer createPlayer(AEPlayer player) throws SQLException;

    AEPlayer updatePlayer(AEPlayer player) throws SQLException;

    Set<AEPlayer> getPlayers() throws SQLException;

    List<AEPlayer> queryPlayers(AEPageQuery query) throws SQLException;

    List<AEPlayer> getPlayersByLastIp(String ip) throws SQLException;

    long countPlayers(AEPageQuery query) throws SQLException;

    AEPlayerBan getActiveBan(String playerUuid) throws SQLException;

    List<AEPlayerBan> getBans(String playerUuid) throws SQLException;

    AEPlayerMute getActiveMute(String playerUuid) throws SQLException;

    List<AEPlayerMute> getMutes(String playerUuid) throws SQLException;

    AEPlayerBan createBan(String playerUuid, String bannedByUuid, String reason, Timestamp expiresAt) throws SQLException;

    AEPlayerMute createMute(String playerUuid, String mutedByUuid, String reason, Timestamp expiresAt) throws SQLException;

    boolean revokeBan(String playerUuid, String revokedByUuid, String reason) throws SQLException;

    boolean revokeMute(String playerUuid, String revokedByUuid, String reason) throws SQLException;

    /** The admin's own Admin Tools settings, or a fresh (never-persisted) default row when none exists yet. */
    AEAdminSettings getOrCreateAdminSettings(String playerUuid) throws SQLException;

    AEAdminSettings saveAdminSettings(AEAdminSettings settings) throws SQLException;

    void invalidate(String playerUuid);

    void clearCache();

    @Override
    void close();
}
