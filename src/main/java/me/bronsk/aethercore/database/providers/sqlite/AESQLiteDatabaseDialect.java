package me.bronsk.aethercore.database.providers.sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;

import java.sql.SQLException;

public class AESQLiteDatabaseDialect implements AEDatabaseDialect {

    /**
     * Ensures that a SQLite table has the requested column.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table to inspect.
     * @param columnName Name of the column to ensure.
     * @param columnDefinition SQL definition used when the column is missing.
     * @throws SQLException when the schema query fails.
     */
    @Override
    public void ensureColumn(Dao<?, ?> dao, String tableName, String columnName, String columnDefinition)
            throws SQLException {
        validateIdentifier(tableName, "table name");
        validateIdentifier(columnName, "column name");

        if (hasColumn(dao, tableName, columnName)) {
            return;
        }

        dao.executeRawNoArgs("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
    }

    @Override
    public void ensureUnsignedLongColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        ensureColumn(dao, tableName, columnName, "INTEGER NOT NULL DEFAULT 0");
    }

    /**
     * Skips runtime foreign key creation for SQLite.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table receiving the foreign key.
     * @param constraintName Name of the foreign key constraint.
     * @param columnName Name of the local foreign key column.
     * @param parentTableName Name of the referenced table.
     * @param parentColumnName Name of the referenced column.
     */
    @Override
    public void ensureRestrictForeignKey(Dao<?, ?> dao, String tableName, String constraintName,
                                         String columnName, String parentTableName, String parentColumnName) {
    }

    /**
     * Closes raw query results and wraps close failures as SQL errors.
     *
     * @param results Raw query results to close.
     * @throws SQLException when closing the results fails.
     */
    private void closeResults(GenericRawResults<String[]> results) throws SQLException {
        try {
            results.close();
        } catch (Exception exception) {
            throw new SQLException("Failed to close SQLite schema metadata query", exception);
        }
    }

    /**
     * Checks whether a SQLite table contains a column.
     *
     * @param dao DAO used to execute the metadata query.
     * @param tableName Name of the table to inspect.
     * @param columnName Name of the column to find.
     * @return True when the column exists.
     * @throws SQLException when the metadata query fails.
     */
    private boolean hasColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        GenericRawResults<String[]> results = dao.queryRaw("PRAGMA table_info(" + tableName + ")");

        try {
            for (String[] row : results) {
                if (row.length > 1 && columnName.equalsIgnoreCase(row[1])) {
                    return true;
                }
            }

            return false;
        } finally {
            closeResults(results);
        }
    }

    /**
     * Validates a SQL identifier used in SQLite PRAGMA statements.
     *
     * @param identifier Identifier to validate.
     * @param fieldName Human-readable field name for the exception.
     */
    private void validateIdentifier(String identifier, String fieldName) {
        if (identifier == null || !identifier.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Invalid SQLite " + fieldName + ": " + identifier);
        }
    }
}
