package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcplayersgroups")
public class AEPlayerGroup {

    public static final String ID_FIELD = "id";
    public static final String PLAYER_UUID_FIELD = "player_uuid";
    public static final String GROUP_ID_FIELD = "group_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = PLAYER_UUID_FIELD, uniqueCombo = true)
    private String playerUuid;

    @DatabaseField(canBeNull = false, columnName = GROUP_ID_FIELD, uniqueCombo = true)
    private int groupId;

    private String groupName;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEPlayerGroup() {
    }

    public AEPlayerGroup(int id, String playerUuid, String groupName, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.groupName = groupName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AEPlayerGroup(int id, String playerUuid, int groupId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

}
