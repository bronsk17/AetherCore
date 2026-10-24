package me.bronsk.aethercore.modules;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.database.AEDatabaseConnection;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.config.AEGlobalConfig;
import me.bronsk.aethercore.database.storage.config.AEModuleConfig;
import me.bronsk.aethercore.database.storage.config.AEStorageConfig;
import me.bronsk.aethercore.events.bus.AEEventBus;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AEModule<C extends AEModuleConfig, S extends AutoCloseable> {

    private final Map<Class<? extends AESubModule<?, ?>>, AESubModule<?, ?>> subModules = new LinkedHashMap<>();
    private C moduleConfig;
    private AEDatabaseConnection databaseConnection;
    private S storage;
    private boolean initialized;

    public abstract String getName();

    /**
     * Initializes resources owned by the module.
     *
     * @throws Exception when initialization fails.
     */
    public final void init() throws Exception {
        loadConfig();
        if (!moduleConfig.isEnabled()) {
            AELogger.logInfo("Module %s is disabled", getName());
            return;
        }
        if (!hasInitializedDependencies()) {
            return;
        }
        initializeResources();
    }

    /**
     * Closes resources owned by the module.
     *
     * @throws Exception when shutdown fails.
     */
    public final void close() throws Exception {
        Exception closeException = null;
        closeException = closeAndAggregate(closeException, this::closeSubModules);
        if (initialized) {
            closeException = closeAndAggregate(closeException, this::onClose);
        }
        closeException = closeAndAggregate(closeException, this::closeStorage);
        initialized = false;

        if (closeException != null) {
            throw closeException;
        }
    }

    /**
     * Adds a logical child module that shares this module's config, storage folder and storage.
     *
     * @param subModule Sub-module instance.
     */
    public final void addSubModule(AESubModule<?, ?> subModule) {
        Objects.requireNonNull(subModule, "subModule");
        if (initialized) {
            throw new IllegalStateException("Cannot add sub-module " + subModule.getName()
                    + " after module " + getName() + " is initialized");
        }
        if (subModule.hasParentModule()) {
            throw new IllegalArgumentException("Sub-module " + subModule.getName() + " already has a parent module");
        }

        @SuppressWarnings("unchecked")
        Class<? extends AESubModule<?, ?>> subModuleClass = (Class<? extends AESubModule<?, ?>>) subModule.getClass();
        if (subModules.containsKey(subModuleClass)) {
            throw new IllegalArgumentException("Sub-module with class "
                    + subModuleClass.getSimpleName() + " is already registered in module " + getName());
        }
        if (hasSubModuleName(subModule.getName())) {
            throw new IllegalArgumentException("Sub-module with name "
                    + subModule.getName() + " is already registered in module " + getName());
        }

        subModule.attachParent(this);
        subModules.put(subModuleClass, subModule);
    }

    /**
     * Returns a registered sub-module by class.
     *
     * @param subModuleClass Sub-module class.
     * @return Registered sub-module instance.
     * @param <T> Sub-module type.
     */
    protected final <T extends AESubModule<?, ?>> T getSubModule(Class<T> subModuleClass) {
        AESubModule<?, ?> subModule = subModules.get(subModuleClass);
        if (subModule == null) {
            throw new IllegalArgumentException("Sub-module with class "
                    + subModuleClass.getSimpleName() + " is not registered in module " + getName());
        }
        return subModuleClass.cast(subModule);
    }

    /**
     * Returns whether this module has completed initialization.
     *
     * @return True when initialized.
     */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns modules that must already be initialized before this module can start.
     *
     * @return Required module classes in dependency order.
     */
    protected List<Class<? extends AEModule<?, ?>>> getRequiredModules() {
        return List.of();
    }

    /**
     * Initializes module-specific resources after config is loaded.
     *
     * @throws Exception when initialization fails.
     */
    protected void onInit() throws Exception {
    }

    /**
     * Closes module-specific resources.
     *
     * @throws Exception when shutdown fails.
     */
    protected void onClose() throws Exception {
    }

    /**
     * Builds this module's storage against the resolved SQL connection (its own config, or the
     * shared global one when this module has no storage override enabled).
     *
     * @param connectionSource Resolved SQL connection source.
     * @param dialect Resolved SQL dialect.
     * @return Module storage.
     * @throws Exception when storage cannot be created.
     */
    protected abstract S createStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws Exception;

    /**
     * Returns initialized module storage.
     *
     * @return Module storage.
     */
    protected final S getStorage() {
        if (storage == null) {
            throw new IllegalStateException("Module " + getName() + " storage is not initialized");
        }
        return storage;
    }

    /**
     * Returns the module config loaded from config.json.
     *
     * @return Module config.
     */
    public C getConfig() {
        if (moduleConfig == null) {
            throw new IllegalStateException("Module " + getName() + " is not initialized");
        }
        return moduleConfig;
    }

    /**
     * Returns the module storage config.
     *
     * @return Storage config.
     */
    protected AEStorageConfig getStorageConfig() {
        return getConfig().getStorage();
    }

    /**
     * Saves this module's config into its own named slice of the shared root config.json - see
     * {@link AEGlobalConfig#getModules()}. A module no longer owns a config file of its own.
     */
    protected void saveConfig() {
        try {
            JsonObject moduleJson = new Gson().toJsonTree(getConfig()).getAsJsonObject();
            getModulesManager().getGlobalConfig().getModules().put(getName(), moduleJson);
            getModulesManager().saveGlobalConfig();
        } catch (Exception ex) {
            AELogger.logException(String.format("Unable to save config for module %s", getName()), ex);
        }
    }

    /**
     * Returns the config class used to load config.json.
     *
     * @return Module config class.
     */
    @SuppressWarnings("unchecked")
    protected Class<C> getConfigClass() {
        Class<?> moduleClass = getClass();
        while (moduleClass != null && moduleClass != AEModule.class) {
            Type genericSuperclass = moduleClass.getGenericSuperclass();
            if (genericSuperclass instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?> rawClass && AEModule.class.isAssignableFrom(rawClass)) {
                    Type configType = parameterizedType.getActualTypeArguments()[0];
                    if (configType instanceof Class<?> configClass && AEModuleConfig.class.isAssignableFrom(configClass)) {
                        return (Class<C>) configClass;
                    }
                }
            }
            moduleClass = moduleClass.getSuperclass();
        }
        return (Class<C>) AEModuleConfig.class;
    }

    /**
     * Creates the default config published when config.json does not exist.
     *
     * @return Default module config.
     * @throws Exception when the config cannot be created.
     */
    protected C createDefaultConfig() throws Exception {
        return getConfigClass().getDeclaredConstructor().newInstance();
    }

    /**
     * Returns the module root folder.
     *
     * @return Module folder.
     */
    protected File getModuleFolder() {
        return new File(AEModulesManager.getBaseDataPath(), getName());
    }

    /**
     * Returns the module storage folder.
     *
     * @return Storage folder.
     */
    protected File getStorageFolder() {
        return getModuleFolder();
    }

    /**
     * Returns the shared module registry.
     *
     * @return Module registry.
     */
    protected AEModulesManager getModulesManager() {
        return AEModulesManager.get();
    }

    /**
     * Registers a listener on the shared AetherCore event bus.
     *
     * @param listener Listener instance to register.
     */
    protected void registerListener(Object listener) {
        AEEventBus.get().register(listener);
    }

    /**
     * Loads this module's own slice of the shared root config.json (see {@link
     * AEGlobalConfig#getModules()}), falling back to a fresh default when that slice isn't there yet
     * (a first run, or a module added after the file already existed) - then writes the resolved
     * config straight back, so a freshly-created default (or one that picked up newly-added fields
     * via Gson's field-by-field merge onto a pre-populated instance) is persisted immediately rather
     * than only on the next explicit {@link #saveConfig()}.
     */
    private void loadConfig() throws Exception {
        C defaultConfig = createDefaultConfig();
        JsonObject moduleJson = getModulesManager().getGlobalConfig().getModules().get(getName());
        if (moduleJson == null) {
            moduleConfig = defaultConfig;
        } else {
            C loaded = new Gson().fromJson(moduleJson, getConfigClass());
            moduleConfig = loaded != null ? loaded : defaultConfig;
        }
        saveConfig();
    }

    private void initializeResources() throws Exception {
        try {
            storage = createConfiguredStorage();
            onInit();
            initialized = true;
            initSubModules();
        } catch (Exception exception) {
            closeAfterInitFailure(exception);
            throw exception;
        }
    }

    private S createConfiguredStorage() throws Exception {
        AEStorageConfig moduleStorageConfig = getStorageConfig();
        return moduleStorageConfig.isEnabled()
                ? createOwnStorage(moduleStorageConfig)
                : createGlobalStorage();
    }

    /** Only a module with its own storage override still needs a folder on disk (for its own database file) - created here, on demand, rather than unconditionally for every module. */
    private S createOwnStorage(AEStorageConfig storageConfig) throws Exception {
        Files.createDirectories(getStorageFolder().toPath());
        databaseConnection = new AEDatabaseConnection(storageConfig.getDatabase(), storageConfig.getType(), getStorageFolder());
        return createStorage(databaseConnection.getConnection(), databaseConnection.getDialect());
    }

    private S createGlobalStorage() throws Exception {
        ConnectionSource connectionSource = getModulesManager().getGlobalConnection();
        AEDatabaseDialect dialect = getModulesManager().getGlobalDialect();
        return createStorage(connectionSource, dialect);
    }

    private void initSubModules() throws Exception {
        for (AESubModule<?, ?> subModule : subModules.values()) {
            subModule.init();
        }
    }

    private void closeSubModules() throws Exception {
        Exception closeException = null;
        List<AESubModule<?, ?>> subModulesToClose = new ArrayList<>(subModules.values());
        Collections.reverse(subModulesToClose);
        for (AESubModule<?, ?> subModule : subModulesToClose) {
            closeException = closeAndAggregate(closeException, subModule::close);
        }

        if (closeException != null) {
            throw closeException;
        }
    }

    private void closeStorage() throws Exception {
        Exception closeException = null;
        if (storage != null) {
            S storageToClose = storage;
            storage = null;
            closeException = closeAndAggregate(closeException, storageToClose::close);
        }

        if (databaseConnection != null) {
            AEDatabaseConnection connectionToClose = databaseConnection;
            databaseConnection = null;
            closeException = closeAndAggregate(closeException, connectionToClose::close);
        }

        if (closeException != null) {
            throw closeException;
        }
    }

    private void closeAfterInitFailure(Exception initException) {
        Exception suppressed = closeAndAggregate(null, this::closeSubModules);
        if (initialized) {
            suppressed = closeAndAggregate(suppressed, this::onClose);
            initialized = false;
        }
        suppressed = closeAndAggregate(suppressed, this::closeStorage);

        if (suppressed != null) {
            initException.addSuppressed(suppressed);
        }
    }

    private boolean hasSubModuleName(String name) {
        for (AESubModule<?, ?> subModule : subModules.values()) {
            if (subModule.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInitializedDependencies() {
        for (Class<? extends AEModule<?, ?>> dependencyClass : getRequiredModules()) {
            if (!getModulesManager().hasModule(dependencyClass)) {
                throw new IllegalStateException("Required module " + dependencyClass.getSimpleName()
                        + " is not registered for module " + getName());
            }

            AEModule<?, ?> dependency = getModulesManager().getModule(dependencyClass);
            if (!dependency.isInitialized()) {
                AELogger.logWarning("Module %s will not be initialized because required module %s is inactive",
                        getName(), dependency.getName());
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    private interface AECloseAction {
        void run() throws Exception;
    }

    private static Exception closeAndAggregate(Exception previous, AECloseAction action) {
        try {
            action.run();
        } catch (Exception exception) {
            if (previous == null) {
                return exception;
            }
            previous.addSuppressed(exception);
        }
        return previous;
    }

}
