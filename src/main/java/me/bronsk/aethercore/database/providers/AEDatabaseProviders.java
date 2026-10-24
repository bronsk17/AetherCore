package me.bronsk.aethercore.database.providers;

import me.bronsk.aethercore.database.providers.mysql.AEMySqlDatabaseProvider;
import me.bronsk.aethercore.database.providers.sqlite.AESQLiteDatabaseProvider;

import java.util.Locale;

public final class AEDatabaseProviders {

    private static final String MYSQL_TYPE = "mysql";
    private static final String SQLITE_TYPE = "sqlite";
    private static final String SQLLITE_TYPE = "sqllite";

    /**
     * Creates a new instance.
     */
    private AEDatabaseProviders() {
    }

    /**
     * Resolves the provider from the configured database type.
     *
     * @param databaseType Configured database type.
     * @return Database provider matching the configured database type.
     */
    public static AEDatabaseProvider resolve(String databaseType) {
        String normalizedType = databaseType == null ? SQLLITE_TYPE : databaseType.trim().toLowerCase(Locale.ROOT);
        if (normalizedType.isBlank()) {
            normalizedType = SQLLITE_TYPE;
        }

        if (MYSQL_TYPE.equals(normalizedType)) {
            return new AEMySqlDatabaseProvider();
        }

        if (SQLITE_TYPE.equals(normalizedType) || SQLLITE_TYPE.equals(normalizedType)) {
            return new AESQLiteDatabaseProvider();
        }

        throw new IllegalArgumentException("Unsupported database type: " + databaseType);
    }
}
