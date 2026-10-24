package me.bronsk.aethercore.database.storage.config;

import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class AEGlobalConfig {

    private String pluginLang = "en-US";
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private AEStorageConfig storage = new AEStorageConfig();

    /**
     * Every module's own config, keyed by module name (e.g. {@code "permissions"}), living inside
     * this single root config.json instead of one file per module - see {@code AEModule#loadConfig}/
     * {@code #saveConfig}. Stored as raw JSON trees here since each module has its own concrete
     * config class; only the owning module ever deserializes its own slice into that class.
     */
    private Map<String, JsonObject> modules = new LinkedHashMap<>();

    public AEGlobalConfig() {
        getStorage().setEnabled(true);
    }

    public String getPluginLang() {
        return pluginLang;
    }

    public void setPluginLang(String pluginLang) {
        this.pluginLang = pluginLang;
    }

    /** The {@link java.time.format.DateTimeFormatter} pattern every date shown anywhere in the plugin should use - see {@code AEDateUtils.format}. */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public AEStorageConfig getStorage() {
        if (storage == null) {
            storage = new AEStorageConfig();
        }
        return storage;
    }

    public Map<String, JsonObject> getModules() {
        if (modules == null) {
            modules = new LinkedHashMap<>();
        }
        return modules;
    }
}
