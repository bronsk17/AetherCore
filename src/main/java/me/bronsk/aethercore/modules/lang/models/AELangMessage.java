package me.bronsk.aethercore.modules.lang.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** A message code's identity - language-agnostic. The actual per-language text lives in {@link AELangMessageTranslation}. */
@DatabaseTable(tableName = "tclangmessages")
public class AELangMessage {

    public static final String ID_FIELD = "id";
    public static final String CODE_FIELD = "code";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = CODE_FIELD)
    private String code;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AELangMessage() {
    }

    public AELangMessage(int id, String code, String description, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
