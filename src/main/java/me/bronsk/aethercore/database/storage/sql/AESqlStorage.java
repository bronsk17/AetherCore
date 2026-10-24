package me.bronsk.aethercore.database.storage.sql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

public abstract class AESqlStorage implements AutoCloseable {

    private final AEDatabaseDialect dialect;
    private final ConnectionSource connectionSource;

    /**
     * Creates SQL storage and initializes its tables.
     *
     * @param connectionSource ORMLite connection source.
     * @param dialect SQL dialect used for schema operations.
     * @throws SQLException when table initialization fails.
     */
    protected AESqlStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws SQLException {
        this.connectionSource = Objects.requireNonNull(connectionSource, "connectionSource");
        this.dialect = Objects.requireNonNull(dialect, "dialect");
        createTables();
    }

    /**
     * Closes the storage and clears its caches.
     */
    @Override
    public void close() {
        clearCache();
    }

    /**
     * Clears storage-local caches.
     */
    protected abstract void clearCache();

    /**
     * Creates or migrates storage tables.
     *
     * @throws SQLException when table initialization fails.
     */
    protected abstract void createTables() throws SQLException;

    protected void ensureColumn(Dao<?, ?> dao, String tableName, String columnName, String columnDefinition)
            throws SQLException {
        dialect.ensureColumn(dao, tableName, columnName, columnDefinition);
    }

    protected void ensureUnsignedLongColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        dialect.ensureUnsignedLongColumn(dao, tableName, columnName);
    }

    protected void ensureRestrictForeignKey(Dao<?, ?> dao, String tableName, String constraintName,
                                            String columnName, String parentTableName,
                                            String parentColumnName) throws SQLException {
        dialect.ensureRestrictForeignKey(dao, tableName, constraintName, columnName, parentTableName, parentColumnName);
    }

    protected ConnectionSource getConnection() {
        return connectionSource;
    }

    protected <T> Dao<T, Integer> getDao(Class<T> daoClass) throws SQLException {
        return DaoManager.createDao(getConnection(), daoClass);
    }

    protected final Timestamp now() {
        return AEDateUtils.now();
    }
}
