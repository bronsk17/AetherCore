package me.bronsk.aethercore.database.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class AEJsonFileStorage<T> {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private final File file;
    private final Class<T> valueClass;

    /**
     * Creates a JSON file storage.
     *
     * @param file JSON file.
     * @param valueClass Stored value class.
     */
    public AEJsonFileStorage(File file, Class<T> valueClass) {
        this.file = Objects.requireNonNull(file, "file");
        this.valueClass = Objects.requireNonNull(valueClass, "valueClass");
    }

    /**
     * Loads a JSON value, creating it from the supplied default when missing.
     *
     * @param defaultValue Default value.
     * @return Loaded value.
     * @throws IOException when the file cannot be read or written.
     */
    public T loadOrCreate(T defaultValue) throws IOException {
        ensureParentFolder();
        if (!file.exists()) {
            save(defaultValue);
            return defaultValue;
        }

        T loaded = GSON.fromJson(Files.readString(file.toPath()), valueClass);
        if (loaded == null) {
            save(defaultValue);
            return defaultValue;
        }
        return loaded;
    }

    /**
     * Saves a JSON value.
     *
     * @param value Value to save.
     * @throws IOException when the file cannot be written.
     */
    public void save(T value) throws IOException {
        ensureParentFolder();
        Files.writeString(file.toPath(), GSON.toJson(value));
    }

    /**
     * Returns the JSON file.
     *
     * @return JSON file.
     */
    public File getFile() {
        return file;
    }

    private void ensureParentFolder() throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
    }
}
