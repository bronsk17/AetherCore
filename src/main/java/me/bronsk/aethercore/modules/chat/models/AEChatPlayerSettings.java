package me.bronsk.aethercore.modules.chat.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcchatplayerssettings")
public class AEChatPlayerSettings extends AEChatSettings {

    public static final String PLAYER_UUID_FIELD = "player_uuid";

    @DatabaseField(canBeNull = false, unique = true, columnName = PLAYER_UUID_FIELD)
    private String playerUuid;

    public AEChatPlayerSettings() {
    }

    public AEChatPlayerSettings(int id, String playerUuid, Timestamp createdAt, Timestamp updatedAt) {
        super(id, createdAt, updatedAt);
        this.playerUuid = playerUuid;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }
}
