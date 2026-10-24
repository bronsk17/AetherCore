package me.bronsk.aethercore.database.providers;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import me.bronsk.aethercore.database.storage.config.AEDatabase;

public final class AEPooledConnectionConfigurator {

    private static final int DEFAULT_MAX_CONNECTIONS_FREE = 16;
    private static final long DEFAULT_MAX_CONNECTION_AGE_MILLIS = 300_000L;
    private static final long DEFAULT_CHECK_CONNECTIONS_EVERY_MILLIS = 60_000L;

    /**
     * Creates a new instance.
     */
    private AEPooledConnectionConfigurator() {
    }

    /**
     * Applies common pool settings to a JDBC pooled connection source.
     *
     * @param source Connection source to configure.
     * @param databaseConfig Database configuration loaded from the plugin config.
     */
    public static void configure(JdbcPooledConnectionSource source, AEDatabase databaseConfig) {
        source.setMaxConnectionsFree(positiveOrDefault(databaseConfig.getMaxConnectionsFree(), DEFAULT_MAX_CONNECTIONS_FREE));
        source.setMaxConnectionAgeMillis(positiveOrDefault(databaseConfig.getMaxConnectionAgeMillis(), DEFAULT_MAX_CONNECTION_AGE_MILLIS));
        source.setCheckConnectionsEveryMillis(positiveOrDefault(databaseConfig.getCheckConnectionsEveryMillis(), DEFAULT_CHECK_CONNECTIONS_EVERY_MILLIS));
        source.setTestBeforeGet(databaseConfig.isTestBeforeGet());
    }

    /**
     * Returns a positive integer value or a default value.
     *
     * @param value Configured value.
     * @param defaultValue Default value used when the configured value is not positive.
     * @return Positive configured value or default value.
     */
    private static int positiveOrDefault(int value, int defaultValue) {
        return value > 0 ? value : defaultValue;
    }

    /**
     * Returns a positive long value or a default value.
     *
     * @param value Configured value.
     * @param defaultValue Default value used when the configured value is not positive.
     * @return Positive configured value or default value.
     */
    private static long positiveOrDefault(long value, long defaultValue) {
        return value > 0 ? value : defaultValue;
    }
}
