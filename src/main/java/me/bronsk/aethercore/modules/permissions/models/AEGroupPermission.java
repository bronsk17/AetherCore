package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** A group's direct permission grant - stores the permission string itself, not a catalog reference. */
@DatabaseTable(tableName = "tcgroupspermissions")
public class AEGroupPermission {

    public static final String ID_FIELD = "id";
    public static final String GROUP_ID_FIELD = "group_id";
    public static final String PERMISSION_FIELD = "permission";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = GROUP_ID_FIELD, uniqueCombo = true)
    private int groupId;

    @DatabaseField(canBeNull = false, columnName = PERMISSION_FIELD, uniqueCombo = true)
    private String permission;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEGroupPermission() {
    }

    public AEGroupPermission(int id, int groupId, String permission, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.permission = permission;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
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
