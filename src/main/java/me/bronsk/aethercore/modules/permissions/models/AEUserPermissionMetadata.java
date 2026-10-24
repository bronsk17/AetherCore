package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** Optional extra data attached to one {@link AEPlayerPermission} row - the world it applies to and/or when it expires. Both are enforced at runtime by {@code AEPermissionsUsersModule#getRuntimeUserPermissions}: an expired grant, or one restricted to a world the player isn't currently in, is filtered out of what the SDK's own resolver sees. */
@DatabaseTable(tableName = "tcuserpermissionmetadata")
public class AEUserPermissionMetadata {

    public static final String ID_FIELD = "id";
    public static final String RELATION_ID_FIELD = "relation_id";
    public static final String WORLD_NAME_FIELD = "world_name";
    public static final String EXPIRES_AT_FIELD = "expires_at";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = RELATION_ID_FIELD)
    private int relationId;

    @DatabaseField(columnName = WORLD_NAME_FIELD)
    private String worldName;

    @DatabaseField(columnName = EXPIRES_AT_FIELD)
    private Timestamp expiresAt;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    /** Not persisted - the raw duration text ("8d") a staged (not-yet-saved) row's expiry was entered as, so reopening its popup shows exactly what was typed instead of a reformatted remaining-time that drifts with the clock. Always null once loaded from the database. */
    private String expiresInText;

    public AEUserPermissionMetadata() {
    }

    public AEUserPermissionMetadata(int id, int relationId, String worldName, Timestamp expiresAt, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.relationId = relationId;
        this.worldName = worldName;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getRelationId() {
        return relationId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
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

    public String getExpiresInText() {
        return expiresInText;
    }

    public void setExpiresInText(String expiresInText) {
        this.expiresInText = expiresInText;
    }
}
