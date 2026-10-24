package me.bronsk.aethercore.modules.lang.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tclangs")
public class AELang {

    public static final String ID_FIELD = "id";
    public static final String CODE_FIELD = "code";
    public static final String DESCRIPTION_FIELD = "description";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = CODE_FIELD)
    private String code;

    @DatabaseField(columnName = DESCRIPTION_FIELD)
    private String description;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    /**
     * Creates a new instance.
     */
    public AELang() {
    }

    /**
     * Creates a new instance.
     *
     * @param id Method argument.
     * @param code Method argument.
     * @param description Method argument.
     * @param createdAt Method argument.
     * @param updatedAt Method argument.
     */
    public AELang(int id, String code, String description, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.code = code;
        this.description = description;
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
     * Returns getCode.
     *
     * @return Method result.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns getDescription.
     *
     * @return Method result.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets setDescription.
     *
     * @param description Method argument.
     */
    public void setDescription(String description) {
        this.description = description;
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
