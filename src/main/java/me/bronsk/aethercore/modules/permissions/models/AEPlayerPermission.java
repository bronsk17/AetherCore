package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** A player's direct permission grant - stores the permission string itself, not a catalog reference. */
@DatabaseTable(tableName = "tcplayerpermissions")
public class AEPlayerPermission {

    public static final String ID_FIELD = "id";
    public static final String PLAYER_UUID_FIELD = "player_uuid";
    public static final String PERMISSION_FIELD = "permission";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = PLAYER_UUID_FIELD, uniqueCombo = true)
    private String playerUuid;

    @DatabaseField(canBeNull = false, columnName = PERMISSION_FIELD, uniqueCombo = true)
    private String permission;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEPlayerPermission() {
    }

    public AEPlayerPermission(int id, String playerUuid, String permission, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.permission = permission;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public String getPermission() {
        return permission;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
