package me.bronsk.aethercore.modules.player.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;

@DatabaseTable(tableName = "tcplayers")
public class AEPlayer {

    public static final String ID_FIELD = "id";
    public static final String UUID_FIELD = "uuid";
    public static final String NAME_FIELD = "name";
    public static final String LAST_IP_FIELD = "last_ip";

    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    private int id;

    @DatabaseField(canBeNull = false, unique = true, columnName = UUID_FIELD)
    private String uuid;

    @DatabaseField(canBeNull = false, columnName = NAME_FIELD)
    private String name;

    @DatabaseField()
    private String lang;

    @DatabaseField(columnName = LAST_IP_FIELD)
    private String lastIp;

    @DatabaseField(columnName = "created_at")
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at")
    private Timestamp updatedAt;

    /**
     * Creates a new instance.
     */
    public AEPlayer() {
    }

    /**
     * Creates a new instance.
     *
     * @param id Method argument.
     * @param uuid Method argument.
     * @param name Method argument.
     * @param createdAt Method argument.
     * @param updatedAt Method argument.
     */
    public AEPlayer(int id, String uuid, String name, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Creates a new "template" instance for a player not yet persisted - id and timestamps are
     * always assigned by storage on create, never by the caller.
     *
     * @param uuid Player UUID.
     * @param name Player name.
     */
    public AEPlayer(String uuid, String name) {
        this(0, uuid, name, null, null);
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
     * Returns getUuid.
     *
     * @return Method result.
     */
    public String getUuid() {
        return uuid;
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
     * Sets setName.
     *
     * @param name Method argument.
     */
    public void setName(String name) {
        this.name = name;
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }
}
