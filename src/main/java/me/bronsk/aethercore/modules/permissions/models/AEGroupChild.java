package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** One edge of the group inheritance DAG: {@code groupId} inherits the permissions of {@code childGroupId}. */
@DatabaseTable(tableName = "tcgroupchildren")
public class AEGroupChild {

    public static final String ID_FIELD = "id";
    public static final String GROUP_ID_FIELD = "group_id";
    public static final String CHILD_GROUP_ID_FIELD = "child_group_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = GROUP_ID_FIELD, uniqueCombo = true)
    private int groupId;

    @DatabaseField(canBeNull = false, columnName = CHILD_GROUP_ID_FIELD, uniqueCombo = true)
    private int childGroupId;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEGroupChild() {
    }

    public AEGroupChild(int id, int groupId, int childGroupId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.childGroupId = childGroupId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getChildGroupId() {
        return childGroupId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
