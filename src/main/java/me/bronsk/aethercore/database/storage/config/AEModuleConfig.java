package me.bronsk.aethercore.database.storage.config;

public class AEModuleConfig {

    private boolean enabled = true;
    private AEStorageConfig storage = new AEStorageConfig();

    /**
     * Creates an enabled module config with default storage.
     */
    public AEModuleConfig() {
    }

    /**
     * Creates an enabled module config with an explicit storage type.
     *
     * @param storageType Storage type.
     */
    public AEModuleConfig(String storageType) {
        this.storage = new AEStorageConfig(storageType);
    }

    /**
     * Returns whether the module should be loaded.
     *
     * @return True when enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the module should be loaded.
     *
     * @param enabled True when enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the module storage config.
     *
     * @return Storage config.
     */
    public AEStorageConfig getStorage() {
        if (storage == null) {
            storage = new AEStorageConfig();
        }
        return storage;
    }

    /**
     * Sets the module storage config.
     *
     * @param storage Storage config.
     */
    public void setStorage(AEStorageConfig storage) {
        this.storage = storage;
    }
}
