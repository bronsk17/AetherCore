package me.bronsk.aethercore.database.storage.config;

public class AEStorageConfig {

    public static final String SQLITE_TYPE = "sqlite";
    public static final String MYSQL_TYPE = "mysql";

    private boolean enabled;
    private String type = SQLITE_TYPE;
    private AEDatabase database = new AEDatabase();

    /**
     * Creates storage config with the default SQL file database.
     */
    public AEStorageConfig() {
    }

    /**
     * Creates storage config with an explicit storage type.
     *
     * @param type Storage type.
     */
    public AEStorageConfig(String type) {
        this.enabled = true;
        this.type = type;
    }

    /**
     * Returns whether this storage config overrides the global storage.
     *
     * @return True when this config should be used directly.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this storage config overrides the global storage.
     *
     * @param enabled True when this config should be used directly.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the configured storage type.
     *
     * @return Storage type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the configured storage type.
     *
     * @param type Storage type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns database settings for SQL-backed storage.
     *
     * @return Database settings.
     */
    public AEDatabase getDatabase() {
        if (database == null) {
            database = new AEDatabase();
        }
        return database;
    }

    /**
     * Sets database settings for SQL-backed storage.
     *
     * @param database Database settings.
     */
    public void setDatabase(AEDatabase database) {
        this.database = database;
    }
}
