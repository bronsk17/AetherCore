package me.bronsk.aethercore.modules.player.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcplayersmutes")
public class AEPlayerMute {

    public static final String ID_FIELD = "id";
    public static final String PLAYER_ID_FIELD = "player_id";
    public static final String MUTED_BY_FIELD = "muted_by";
    public static final String REVOKED_BY_FIELD = "revoked_by";
    public static final String REVOKED_AT_FIELD = "revoked_at";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = PLAYER_ID_FIELD)
    private int playerId;

    @DatabaseField(columnName = MUTED_BY_FIELD)
    private Integer mutedBy;

    @DatabaseField(columnName = REVOKED_BY_FIELD)
    private Integer revokedBy;

    @DatabaseField(canBeNull = false)
    private String reason;

    @DatabaseField(columnName = "expires_at")
    private Timestamp expiresAt;

    @DatabaseField(columnName = REVOKED_AT_FIELD)
    private Timestamp revokedAt;

    @DatabaseField(columnName = "revoked_reason")
    private String revokedReason;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AEPlayerMute() {
    }

    public AEPlayerMute(int id, int playerId, Integer mutedBy, String reason,
                        Timestamp expiresAt, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.playerId = playerId;
        this.mutedBy = mutedBy;
        this.reason = reason;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Integer getMutedBy() {
        return mutedBy;
    }

    public Integer getRevokedBy() {
        return revokedBy;
    }

    public void setRevokedBy(Integer revokedBy) {
        this.revokedBy = revokedBy;
    }

    public String getReason() {
        return reason;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public Timestamp getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Timestamp revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getRevokedReason() {
        return revokedReason;
    }

    public void setRevokedReason(String revokedReason) {
        this.revokedReason = revokedReason;
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

    public boolean isActive() {
        return revokedAt == null && (expiresAt == null || expiresAt.after(new Timestamp(System.currentTimeMillis())));
    }
}
