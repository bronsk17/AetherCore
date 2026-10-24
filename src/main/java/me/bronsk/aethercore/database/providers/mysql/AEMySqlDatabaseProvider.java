package me.bronsk.aethercore.database.providers.mysql;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.providers.AEDatabaseProvider;
import me.bronsk.aethercore.database.providers.AEPooledConnectionConfigurator;
import me.bronsk.aethercore.database.storage.config.AEDatabase;

import java.io.File;
import java.sql.SQLException;

public class AEMySqlDatabaseProvider implements AEDatabaseProvider {

    private static final String JDBC_PREFIX = "jdbc:";
    private static final String MYSQL_PREFIX = "jdbc:mysql://";
    private static final String DEFAULT_PARAMETERS = "useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private final AEDatabaseDialect dialect = new AEMySqlDatabaseDialect();

    /**
     * Creates a MySQL connection source.
     *
     * @param databaseConfig Database configuration loaded from the plugin config.
     * @param dataFolder Plugin data directory.
     * @return Initialized ORMLite connection source.
     * @throws SQLException when the connection cannot be created.
     */
    @Override
    public ConnectionSource createConnection(AEDatabase databaseConfig, File dataFolder) throws SQLException {
        JdbcPooledConnectionSource source = new JdbcPooledConnectionSource();
        source.setUrl(buildJdbcUrl(databaseConfig));
        source.setUsername(databaseConfig.getUsername());
        source.setPassword(databaseConfig.getPassword());
        AEPooledConnectionConfigurator.configure(source, databaseConfig);
        source.initialize();
        return source;
    }

    /**
     * Returns the MySQL dialect.
     *
     * @return MySQL database dialect.
     */
    @Override
    public AEDatabaseDialect getDialect() {
        return dialect;
    }

    /**
     * Adds default MySQL parameters when the URL has no query string.
     *
     * @param jdbcUrl JDBC URL to complete.
     * @return JDBC URL with default parameters.
     */
    private String appendDefaultParameters(String jdbcUrl) {
        if (jdbcUrl.contains("?")) {
            return jdbcUrl;
        }

        return jdbcUrl + "?" + DEFAULT_PARAMETERS;
    }

    /**
     * Builds the MySQL JDBC URL from the database config.
     *
     * @param databaseConfig Database configuration loaded from the plugin config.
     * @return MySQL JDBC URL.
     */
    private String buildJdbcUrl(AEDatabase databaseConfig) {
        String address = requireValue(databaseConfig.getAddress(), "database address");
        String databaseName = requireValue(databaseConfig.getName(), "database name");

        if (address.startsWith(JDBC_PREFIX)) {
            return appendDefaultParameters(address);
        }

        return appendDefaultParameters(MYSQL_PREFIX + address + "/" + databaseName);
    }

    /**
     * Requires a non-blank configuration value.
     *
     * @param value Value to validate.
     * @param fieldName Human-readable field name for the exception.
     * @return Validated value.
     */
    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + fieldName + " in database config");
        }

        return value;
    }
}
