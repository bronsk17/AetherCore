package me.bronsk.aethercore.modules.player.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** One admin's own Admin Tools preferences (vanish/gamemode/spectator/freecam/speed) - reapplied on every join, edited from the personal "Admin Tools" tab. */
@DatabaseTable(tableName = "aeadminsettings")
public class AEAdminSettings {

    public static final String ID_FIELD = "id";
    public static final String PLAYER_ID_FIELD = "player_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = PLAYER_ID_FIELD)
    private int playerId;

    @DatabaseField(columnName = "is_vanish")
    private boolean vanish;

    /** "Adventure" or "Creative" - the base mode to return to when spectator is turned off. */
    @DatabaseField(columnName = "gamemode")
    private String gamemode = "Adventure";

    @DatabaseField(columnName = "is_spectator")
    private boolean spectator;

    @DatabaseField(columnName = "is_freecam")
    private boolean freeCam;

    /** Abstract level, not a direct multiplier - see {@code AEGamePlayer#setSpeedLevel}. 1 = normal (walking) speed. */
    @DatabaseField(columnName = "speed_level")
    private int speedLevel = 1;

    /** Same abstract-level shape as {@link #speedLevel}, but for flying (fly-camera/spectator) speed - see {@code AEGamePlayer#setFlySpeedLevel}. Independent from walking speed since the native {@code MovementSettings} tracks them as separate fields ({@code baseSpeed} vs {@code horizontalFlySpeed}/{@code verticalFlySpeed}). */
    @DatabaseField(columnName = "fly_speed_level")
    private int flySpeedLevel = 1;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEAdminSettings() {
    }

    public AEAdminSettings(int id, int playerId, boolean vanish, String gamemode, boolean spectator,
                            boolean freeCam, int speedLevel, int flySpeedLevel, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.vanish = vanish;
        this.gamemode = gamemode;
        this.spectator = spectator;
        this.freeCam = freeCam;
        this.speedLevel = speedLevel;
        this.flySpeedLevel = flySpeedLevel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public boolean isVanish() {
        return vanish;
    }

    public void setVanish(boolean vanish) {
        this.vanish = vanish;
    }

    public String getGamemode() {
        return gamemode;
    }

    public void setGamemode(String gamemode) {
        this.gamemode = gamemode;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public boolean isFreeCam() {
        return freeCam;
    }

    public void setFreeCam(boolean freeCam) {
        this.freeCam = freeCam;
    }

    public int getSpeedLevel() {
        return speedLevel;
    }

    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }

    public int getFlySpeedLevel() {
        return flySpeedLevel;
    }

    public void setFlySpeedLevel(int flySpeedLevel) {
        this.flySpeedLevel = flySpeedLevel;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
