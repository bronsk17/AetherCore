package me.bronsk.aethercore.database.providers;

import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.database.storage.config.AEDatabase;

import java.io.File;
import java.sql.SQLException;

public interface AEDatabaseProvider {

    /**
     * Creates and initializes a database connection source.
     *
     * @param databaseConfig Database configuration loaded from the plugin config.
     * @param dataFolder Plugin data directory.
     * @return Initialized ORMLite connection source.
     * @throws SQLException when the connection cannot be created.
     */
    ConnectionSource createConnection(AEDatabase databaseConfig, File dataFolder) throws SQLException;

    /**
     * Returns the SQL dialect used by this provider.
     *
     * @return Database dialect for schema operations.
     */
    AEDatabaseDialect getDialect();
}
