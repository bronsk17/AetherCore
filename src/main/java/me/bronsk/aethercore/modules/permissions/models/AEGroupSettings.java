package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcgroupssettings")
public class AEGroupSettings {

    public static final String ID_FIELD = "id";
    public static final String GROUP_ID_FIELD = "group_id";
    public static final String PREFIX_FIELD = "prefix";
    public static final String SUFFIX_FIELD = "suffix";
    public static final String PRIORITY_FIELD = "priority";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = GROUP_ID_FIELD)
    private int groupId;

    @DatabaseField(columnName = PREFIX_FIELD)
    private String prefix;

    @DatabaseField(columnName = SUFFIX_FIELD)
    private String suffix;

    @DatabaseField(canBeNull = false, defaultValue = "0", columnName = PRIORITY_FIELD)
    private int priority;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEGroupSettings() {
    }

    public AEGroupSettings(int id, int groupId, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getGroupId() {
        return groupId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
