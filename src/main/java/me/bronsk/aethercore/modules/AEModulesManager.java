package me.bronsk.aethercore.modules;

import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.database.AEDatabaseConnection;
import me.bronsk.aethercore.database.AEDatabaseInstaller;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.config.AEGlobalConfig;
import me.bronsk.aethercore.database.storage.config.AELegacyDatabaseNames;
import me.bronsk.aethercore.database.storage.config.AEStorageConfig;
import me.bronsk.aethercore.database.storage.file.AEJsonFileStorage;

import com.j256.ormlite.support.ConnectionSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AEModulesManager {

    private static final AEModulesManager INSTANCE = new AEModulesManager();

    private final Map<Class<? extends AEModule<?, ?>>, AEModule<?, ?>> modules = new LinkedHashMap<>();
    private final AEDatabaseInstaller databaseInstaller = new AEDatabaseInstaller();
    private AEGlobalConfig globalConfig;
    private AEDatabaseConnection globalDatabaseConnection;

    /**
     * Returns the shared module registry.
     *
     * @return Module registry singleton.
     */
    public static AEModulesManager get() {
        return INSTANCE;
    }

    /**
     * Returns a registered module by class.
     *
     * @param moduleClass Module class.
     * @return Registered module instance.
     * @param <T> Module type.
     */
    public <T extends AEModule<?, ?>> T getModule(Class<T> moduleClass) {
        AEModule<?, ?> module = modules.get(moduleClass);
        if (module == null) {
            throw new IllegalArgumentException(String.format(
                    "Module with class %s is not registered",
                    moduleClass.getSimpleName()));
        }
        return moduleClass.cast(module);
    }

    /**
     * Checks whether a module is registered.
     *
     * @param moduleClass Module class.
     * @return True when the module is registered.
     */
    public boolean hasModule(Class<? extends AEModule<?, ?>> moduleClass) {
        return modules.containsKey(moduleClass);
    }

    /**
     * Registers a module instance.
     *
     * @param moduleInstance Module instance.
     */
    public void registerModule(AEModule<?, ?> moduleInstance) {
        @SuppressWarnings("unchecked")
        Class<? extends AEModule<?, ?>> moduleClass = (Class<? extends AEModule<?, ?>>) moduleInstance.getClass();
        if (modules.containsKey(moduleClass)) {
            throw new IllegalArgumentException(String.format(
                    "Module with class %s is already registered",
                    moduleClass.getSimpleName()));
        }
        modules.put(moduleClass, moduleInstance);
    }

    /**
     * Initializes registered modules in registration order.
     *
     * @throws Exception when a module cannot initialize.
     */
    public void initModules() throws Exception {
        loadGlobalConfig();
        for (AEModule<?, ?> module : modules.values()) {
            module.init();
        }
        logUninitializedModules();
    }

    private void logUninitializedModules() {
        List<String> inactive = modules.values().stream()
                .filter(module -> !module.isInitialized())
                .map(AEModule::getName)
                .toList();
        if (!inactive.isEmpty()) {
            AELogger.logWarning("The following modules did not initialize and are inactive: %s", String.join(", ", inactive));
        }
    }

    /**
     * Closes registered modules in reverse registration order.
     *
     * @throws Exception when a module cannot close.
     */
    public void closeModules() throws Exception {
        Exception closeException = null;
        List<AEModule<?, ?>> moduleInstances = new ArrayList<>(modules.values());
        Collections.reverse(moduleInstances);
        for (AEModule<?, ?> module : moduleInstances) {
            try {
                module.close();
            } catch (Exception exception) {
                if (closeException == null) {
                    closeException = exception;
                } else {
                    closeException.addSuppressed(exception);
                }
            }
        }

        try {
            closeGlobalDatabaseConnection();
        } catch (Exception exception) {
            if (closeException == null) {
                closeException = exception;
            } else {
                closeException.addSuppressed(exception);
            }
        }

        if (closeException != null) {
            throw closeException;
        }
    }

    /**
     * Clears all registered modules.
     */
    public void clear() {
        modules.clear();
        databaseInstaller.clear();
        globalConfig = null;
        try {
            closeGlobalDatabaseConnection();
        } catch (Exception exception) {
            AELogger.logException("Failed to close global database connection", exception);
        }
    }

    /**
     * Returns the registry of default database installers owned by this manager.
     *
     * @return Database installer registry.
     */
    public AEDatabaseInstaller getDatabaseInstaller() {
        return databaseInstaller;
    }

    /**
     * Returns the global config loaded from the root config.json.
     *
     * @return Global config.
     */
    public AEGlobalConfig getGlobalConfig() {
        if (globalConfig == null) {
            throw new IllegalStateException("Global config is not loaded");
        }
        return globalConfig;
    }

    /**
     * Persists the current in-memory global config - including every module's own slice under
     * {@link AEGlobalConfig#getModules()} - back to the single shared root config.json. Called by
     * {@code AEModule#saveConfig} whenever a module's own config changes, since a module no longer
     * owns a config file of its own to write to.
     */
    public void saveGlobalConfig() {
        try {
            new AEJsonFileStorage<>(getGlobalConfigFile(), AEGlobalConfig.class).save(getGlobalConfig());
        } catch (Exception exception) {
            AELogger.logException("Failed to save root config.json", exception);
        }
    }

    private File getGlobalConfigFile() {
        return new File(getBaseFolder(), "config.json");
    }

    /**
     * Returns the root folder for AetherCore persistent files.
     *
     * @return Root data folder.
     */
    public File getBaseFolder() {
        return new File(getBaseDataPath());
    }

    /**
     * Returns the mods-relative data path for AetherCore, computed lazily so it doesn't
     * depend on {@link AetherPlugin#getPlugin()} being ready at class-load time.
     *
     * @return Base data path.
     */
    public static String getBaseDataPath() {
        return "mods/" +
                AetherPlugin.getPlugin().getIdentifier().getGroup() +
                "_" + AetherPlugin.getPlugin().getIdentifier().getName();
    }

    /**
     * Returns the global SQL connection source, creating it when needed.
     *
     * @return Global SQL connection source.
     * @throws Exception when the connection cannot be created.
     */
    public ConnectionSource getGlobalConnection() throws Exception {
        return getGlobalDatabaseConnection().getConnection();
    }

    /**
     * Returns the global SQL dialect.
     *
     * @return Global SQL dialect.
     */
    public AEDatabaseDialect getGlobalDialect() {
        return getGlobalDatabaseConnection().getDialect();
    }

    private void loadGlobalConfig() throws Exception {
        AEGlobalConfig defaultConfig = new AEGlobalConfig();
        AEJsonFileStorage<AEGlobalConfig> storage = new AEJsonFileStorage<>(getGlobalConfigFile(), AEGlobalConfig.class);
        globalConfig = storage.loadOrCreate(defaultConfig);
        globalConfig.getStorage().setEnabled(true);
        if (applyGlobalDefaults(globalConfig)) {
            storage.save(globalConfig);
        }
    }

    private boolean applyGlobalDefaults(AEGlobalConfig config) {
        AEStorageConfig storageConfig = config.getStorage();
        boolean changed = false;
        if (!storageConfig.isEnabled()) {
            storageConfig.setEnabled(true);
            changed = true;
        }

        if (AEStorageConfig.SQLITE_TYPE.equals(normalizeType(storageConfig.getType()))) {
            String databaseName = storageConfig.getDatabase().getName();
            if (AELegacyDatabaseNames.isLegacyOrBlank(databaseName) && !"torvexcore.db".equalsIgnoreCase(databaseName)) {
                storageConfig.getDatabase().setName("torvexcore.db");
                changed = true;
            }
        }
        return changed;
    }

    private AEDatabaseConnection getGlobalDatabaseConnection() {
        if (globalDatabaseConnection == null) {
            AEStorageConfig storageConfig = getGlobalConfig().getStorage();
            globalDatabaseConnection = new AEDatabaseConnection(
                    storageConfig.getDatabase(),
                    storageConfig.getType(),
                    getBaseFolder());
        }
        return globalDatabaseConnection;
    }

    private void closeGlobalDatabaseConnection() throws Exception {
        if (globalDatabaseConnection == null) {
            return;
        }

        globalDatabaseConnection.close();
        globalDatabaseConnection = null;
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toLowerCase();
    }

    private AEModulesManager() {
    }

}
