package me.bronsk.aethercore.modules.permissions.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcgroups")
public class AEGroup {

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";
    public static final String IS_DEFAULT_FIELD = "is_default";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = NAME_FIELD)
    private String name;

    @DatabaseField(canBeNull = false, defaultValue = "0", columnName = IS_DEFAULT_FIELD)
    private boolean isDefault;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    /**
     * Creates a new instance.
     */
    public AEGroup() {
    }

    /**
     * Creates a new instance.
     *
     * @param id Method argument.
     * @param name Method argument.
     * @param createdAt Method argument.
     * @param updatedAt Method argument.
     */
    public AEGroup(int id, String name, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.isDefault = false;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Returns getId.
     *
     * @return Method result.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns getName.
     *
     * @return Method result.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns getDefault.
     *
     * @return Method result.
     */
    public boolean isDefault() {
        return isDefault;
    }

    /**
     * Sets setDefaultGroup.
     *
     * @param isDefault Method argument.
     */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * Returns getCreatedAt.
     *
     * @return Method result.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns getUpdatedAt.
     *
     * @return Method result.
     */
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets setUpdatedAt.
     *
     * @param updatedAt Method argument.
     */
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

}
