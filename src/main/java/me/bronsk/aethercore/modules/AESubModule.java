package me.bronsk.aethercore.modules;

import me.bronsk.aethercore.events.bus.AEEventBus;

import me.bronsk.aethercore.database.storage.config.AEModuleConfig;

import java.util.Objects;

public abstract class AESubModule<P extends AEModule<?, S>, S extends AutoCloseable> {

    private P parentModule;
    private boolean initialized;

    /**
     * Returns the stable sub-module name.
     *
     * @return Sub-module name.
     */
    public abstract String getName();

    /**
     * Initializes resources owned by the sub-module.
     *
     * @throws Exception when initialization fails.
     */
    public final void init() throws Exception {
        onInit();
        initialized = true;
    }

    /**
     * Closes resources owned by the sub-module.
     *
     * @throws Exception when shutdown fails.
     */
    public final void close() throws Exception {
        if (initialized) {
            try {
                onClose();
            } finally {
                initialized = false;
            }
        }
    }

    /**
     * Initializes sub-module-specific resources.
     *
     * @throws Exception when initialization fails.
     */
    protected void onInit() throws Exception {
    }

    /**
     * Closes sub-module-specific resources.
     *
     * @throws Exception when shutdown fails.
     */
    protected void onClose() throws Exception {
    }

    /**
     * Returns the parent module.
     *
     * @return Parent module.
     */
    protected final P getParentModule() {
        if (parentModule == null) {
            throw new IllegalStateException("Sub-module " + getName() + " has no parent module");
        }
        return parentModule;
    }

    /**
     * Returns the storage shared with the parent module.
     *
     * @return Shared module storage.
     */
    protected final S getStorage() {
        return getParentModule().getStorage();
    }

    /**
     * Returns the parent module config.
     *
     * @return Parent module config.
     */
    protected final AEModuleConfig getParentConfig() {
        return getParentModule().getConfig();
    }

    /**
     * Returns the shared module registry.
     *
     * @return Module registry.
     */
    protected final AEModulesManager getModulesManager() {
        return getParentModule().getModulesManager();
    }

    /**
     * Returns whether this sub-module has completed initialization.
     *
     * @return True when initialized.
     */
    public final boolean isInitialized() {
        return initialized;
    }

    /**
     * Registers a listener on the shared AetherCore event bus.
     *
     * @param listener Listener instance to register.
     */
    protected void registerListener(Object listener) {
        AEEventBus.get().register(listener);
    }

    @SuppressWarnings("unchecked")
    final void attachParent(AEModule<?, ?> parentModule) {
        if (this.parentModule != null) {
            throw new IllegalStateException("Sub-module " + getName() + " already has a parent module");
        }
        this.parentModule = (P) Objects.requireNonNull(parentModule, "parentModule");
    }

    final boolean hasParentModule() {
        return parentModule != null;
    }
}
