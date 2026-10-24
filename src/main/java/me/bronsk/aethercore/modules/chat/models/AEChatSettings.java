package me.bronsk.aethercore.modules.chat.models;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

import java.sql.Timestamp;

/** Shared chat-setting fields for a group or a player - each subclass only adds its own identity field and table. */
public abstract class AEChatSettings {

    public static final String ID_FIELD = "id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(columnName = "format")
    private String format;

    @SerializedName(value = "serverJoinMessageCode", alternate = "joinMessage")
    @DatabaseField(columnName = "server_join_message")
    private String serverJoinMessageCode;

    @DatabaseField(columnName = "player_join_message")
    private String playerJoinMessageCode;

    @DatabaseField(columnName = "leave_message")
    private String leaveMessageCode;

    @DatabaseField(columnName = "first_join_message")
    private String firstJoinMessageCode;

    @DatabaseField(columnName = "broadcast_first_join_message")
    private Boolean broadcastFirstJoinMessage;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    protected AEChatSettings() {
    }

    protected AEChatSettings(int id, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getServerJoinMessageCode() {
        return serverJoinMessageCode;
    }

    public void setServerJoinMessageCode(String serverJoinMessageCode) {
        this.serverJoinMessageCode = serverJoinMessageCode;
    }

    public String getPlayerJoinMessageCode() {
        return playerJoinMessageCode;
    }

    public void setPlayerJoinMessageCode(String playerJoinMessageCode) {
        this.playerJoinMessageCode = playerJoinMessageCode;
    }

    public String getLeaveMessageCode() {
        return leaveMessageCode;
    }

    public void setLeaveMessageCode(String leaveMessageCode) {
        this.leaveMessageCode = leaveMessageCode;
    }

    public String getFirstJoinMessageCode() {
        return firstJoinMessageCode;
    }

    public void setFirstJoinMessageCode(String firstJoinMessageCode) {
        this.firstJoinMessageCode = firstJoinMessageCode;
    }

    public Boolean getBroadcastFirstJoinMessage() {
        return broadcastFirstJoinMessage;
    }

    public void setBroadcastFirstJoinMessage(Boolean broadcastFirstJoinMessage) {
        this.broadcastFirstJoinMessage = broadcastFirstJoinMessage;
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
