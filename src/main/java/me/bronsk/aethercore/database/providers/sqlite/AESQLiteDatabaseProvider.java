package me.bronsk.aethercore.database.providers.sqlite;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.providers.AEDatabaseProvider;
import me.bronsk.aethercore.database.providers.AEPooledConnectionConfigurator;
import me.bronsk.aethercore.database.storage.config.AEDatabase;

import java.io.File;
import java.sql.SQLException;

public class AESQLiteDatabaseProvider implements AEDatabaseProvider {

    private static final String JDBC_SQLITE_PREFIX = "jdbc:sqlite:";
    private static final String SQLITE_PREFIX = "sqlite:";
    private static final String SQLITE_FILE_EXTENSION = ".db";
    private final AEDatabaseDialect dialect = new AESQLiteDatabaseDialect();

    /**
     * Creates a SQLite connection source.
     *
     * @param databaseConfig Database configuration loaded from the plugin config.
     * @param dataFolder Plugin data directory.
     * @return Initialized ORMLite connection source.
     * @throws SQLException when the connection cannot be created.
     */
    @Override
    public ConnectionSource createConnection(AEDatabase databaseConfig, File dataFolder) throws SQLException {
        JdbcPooledConnectionSource source = new JdbcPooledConnectionSource();
        source.setUrl(buildJdbcUrl(databaseConfig, dataFolder));
        AEPooledConnectionConfigurator.configure(source, databaseConfig);
        source.initialize();
        return source;
    }

    /**
     * Returns the SQLite dialect.
     *
     * @return SQLite database dialect.
     */
    @Override
    public AEDatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * Builds the SQLite JDBC URL from the database config.
     *
     * @param databaseConfig Database configuration loaded from the plugin config.
     * @param dataFolder Plugin data directory.
     * @return SQLite JDBC URL.
     */
    private String buildJdbcUrl(AEDatabase databaseConfig, File dataFolder) {
        String address = databaseConfig.getAddress();
        String normalizedAddress = address == null ? null : address.toLowerCase();
        if (normalizedAddress != null && normalizedAddress.startsWith(JDBC_SQLITE_PREFIX)) {
            return address;
        }

        if (normalizedAddress != null && normalizedAddress.startsWith(SQLITE_PREFIX)) {
            return JDBC_SQLITE_PREFIX + address.substring(SQLITE_PREFIX.length());
        }

        String databaseName = databaseConfig.getName();
        if (databaseName == null || databaseName.isBlank()) {
            databaseName = "torvexcore.db";
        }

        if (!databaseName.endsWith(SQLITE_FILE_EXTENSION)) {
            databaseName = databaseName + SQLITE_FILE_EXTENSION;
        }

        File databaseFile = new File(databaseName);
        if (!databaseFile.isAbsolute()) {
            databaseFile = new File(dataFolder, databaseName);
        }

        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Unable to create SQLite database directory: " + parent.getAbsolutePath());
        }

        return JDBC_SQLITE_PREFIX + databaseFile.getAbsolutePath();
    }
}
