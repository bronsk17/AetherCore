package me.bronsk.aethercore.database;

import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.database.storage.config.AEDatabase;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.providers.AEDatabaseProvider;
import me.bronsk.aethercore.database.providers.AEDatabaseProviders;

import java.io.File;
import java.sql.SQLException;
import java.util.Objects;

public class AEDatabaseConnection implements AutoCloseable {

    private final AEDatabase databaseConfig;
    private final File dataFolder;
    private final AEDatabaseProvider provider;
    private ConnectionSource connectionSource;

    /**
     * Creates a database connection wrapper.
     *
     * @param databaseConfig Database connection configuration loaded from the plugin config.
     * @param databaseType Database provider type loaded from the plugin config.
     * @param dataFolder Plugin data directory.
     */
    public AEDatabaseConnection(AEDatabase databaseConfig, String databaseType, File dataFolder) {
        this.databaseConfig = Objects.requireNonNull(databaseConfig, "databaseConfig");
        this.dataFolder = Objects.requireNonNull(dataFolder, "dataFolder");
        this.provider = AEDatabaseProviders.resolve(databaseType);
    }

    /**
     * Returns the active connection source, creating it when needed.
     *
     * @return Active ORMLite connection source.
     * @throws SQLException when the connection cannot be created.
     */
    public synchronized ConnectionSource getConnection() throws SQLException {
        if (isConnected()) {
            return connectionSource;
        }

        connectionSource = provider.createConnection(databaseConfig, dataFolder);
        return connectionSource;
    }

    /**
     * Returns the provider SQL dialect.
     *
     * @return SQL dialect for schema operations.
     */
    public AEDatabaseDialect getDialect() {
        return provider.getDialect();
    }

    /**
     * Checks whether a connection source has been created.
     *
     * @return True when the connection source is active.
     */
    public synchronized boolean isConnected() {
        return connectionSource != null;
    }

    /**
     * Closes the active connection source.
     *
     * @throws Exception when closing the connection source fails.
     */
    @Override
    public synchronized void close() throws Exception {
        if (connectionSource == null) {
            return;
        }

        connectionSource.close();
        connectionSource = null;
    }
}
