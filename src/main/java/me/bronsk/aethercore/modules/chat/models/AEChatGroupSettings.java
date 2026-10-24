package me.bronsk.aethercore.modules.chat.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcchatgroupssettings")
public class AEChatGroupSettings extends AEChatSettings {

    public static final String GROUP_NAME_FIELD = "group_name";

    @DatabaseField(canBeNull = false, unique = true, columnName = GROUP_NAME_FIELD)
    private String groupName;

    public AEChatGroupSettings() {
    }

    public AEChatGroupSettings(int id, String groupName, Timestamp createdAt, Timestamp updatedAt) {
        super(id, createdAt, updatedAt);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
