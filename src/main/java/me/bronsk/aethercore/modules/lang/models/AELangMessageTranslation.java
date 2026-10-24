package me.bronsk.aethercore.modules.lang.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

/** One language's translated text for one {@link AELangMessage}. */
@DatabaseTable(tableName = "tclangmessagetranslations")
public class AELangMessageTranslation {

    public static final String ID_FIELD = "id";
    public static final String LANG_ID_FIELD = "lang_id";
    public static final String MESSAGE_ID_FIELD = "message_id";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, columnName = LANG_ID_FIELD, uniqueCombo = true)
    private int langId;

    @DatabaseField(canBeNull = false, columnName = MESSAGE_ID_FIELD, uniqueCombo = true)
    private int messageId;

    @DatabaseField(canBeNull = false, columnName = "text")
    private String text;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    public AELangMessageTranslation() {
    }

    public AELangMessageTranslation(int id, int langId, int messageId, String text, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.langId = langId;
        this.messageId = messageId;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public int getLangId() {
        return langId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
