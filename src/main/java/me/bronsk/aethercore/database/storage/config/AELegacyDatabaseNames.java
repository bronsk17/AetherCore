package me.bronsk.aethercore.database.storage.config;

import java.util.Locale;
import java.util.Set;

/** Legacy default SQLite database names still recognized so old config.json files migrate cleanly. */
public final class AELegacyDatabaseNames {

    private static final Set<String> LEGACY_NAMES = Set.of(
            "bronskcommons", "bronskcommons.db", "torvexcore", "torvexcore.db");

    private AELegacyDatabaseNames() {
    }

    /**
     * Checks whether a configured database name is blank or one of the recognized legacy defaults.
     *
     * @param databaseName Configured database name.
     * @return True when the name should be replaced with the current default.
     */
    public static boolean isLegacyOrBlank(String databaseName) {
        if (databaseName == null || databaseName.isBlank()) {
            return true;
        }
        return LEGACY_NAMES.contains(databaseName.trim().toLowerCase(Locale.ROOT));
    }
}
