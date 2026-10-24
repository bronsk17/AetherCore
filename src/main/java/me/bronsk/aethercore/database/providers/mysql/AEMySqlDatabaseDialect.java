package me.bronsk.aethercore.database.providers.mysql;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;

import java.sql.SQLException;

public class AEMySqlDatabaseDialect implements AEDatabaseDialect {

    /**
     * Ensures that a MySQL table has the requested column.
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
        if (hasColumn(dao, tableName, columnName)) {
            return;
        }

        dao.executeRawNoArgs("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
    }

    @Override
    public void ensureUnsignedLongColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        if (!hasColumn(dao, tableName, columnName)) {
            dao.executeRawNoArgs("ALTER TABLE " +tableName +" ADD COLUMN " +columnName +" BIGINT UNSIGNED NOT NULL DEFAULT 0");
            return;
        }

        if (isUnsignedLongColumn(dao, tableName, columnName)) {
            return;
        }

        dao.executeRawNoArgs("ALTER TABLE " +tableName +" MODIFY COLUMN " +columnName +" BIGINT UNSIGNED NOT NULL DEFAULT 0");
    }

    /**
     * Ensures that a MySQL table has a restrict foreign key.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table receiving the foreign key.
     * @param constraintName Name of the foreign key constraint.
     * @param columnName Name of the local foreign key column.
     * @param parentTableName Name of the referenced table.
     * @param parentColumnName Name of the referenced column.
     * @throws SQLException when the schema query fails.
     */
    @Override
    public void ensureRestrictForeignKey(Dao<?, ?> dao, String tableName, String constraintName, String columnName, String parentTableName, String parentColumnName)
            throws SQLException {
        if (hasForeignKey(dao, tableName, constraintName, columnName, parentTableName, parentColumnName)) {
            return;
        }

        try {
            dao.executeRawNoArgs(
                    "ALTER TABLE " +tableName +" ADD CONSTRAINT " +constraintName +" FOREIGN KEY (" +columnName +")" +" REFERENCES " +parentTableName +"(" +parentColumnName +")" +" ON DELETE RESTRICT ON UPDATE RESTRICT");
        } catch (SQLException exception) {
            if (hasForeignKey(dao, tableName, constraintName, columnName, parentTableName, parentColumnName)) {
                return;
            }

            throw exception;
        }
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
            throw new SQLException("Failed to close schema metadata query", exception);
        }
    }

    /**
     * Checks whether a MySQL table contains a column.
     *
     * @param dao DAO used to execute the metadata query.
     * @param tableName Name of the table to inspect.
     * @param columnName Name of the column to find.
     * @return True when the column exists.
     * @throws SQLException when the metadata query fails.
     */
    private boolean hasColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        GenericRawResults<String[]> results = dao.queryRaw(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +"WHERE TABLE_SCHEMA = DATABASE() " +"AND TABLE_NAME = ? " +"AND COLUMN_NAME = ?", tableName, columnName);

        try {
            return results.iterator().hasNext();
        } finally {
            closeResults(results);
        }
    }

    private boolean isUnsignedLongColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException {
        GenericRawResults<String[]> results = dao.queryRaw(
                "SELECT DATA_TYPE, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +"WHERE TABLE_SCHEMA = DATABASE() " +"AND TABLE_NAME = ? " +"AND COLUMN_NAME = ?", tableName, columnName);

        try {
            for (String[] row : results) {
                if (row.length > 1) {
                    return "bigint".equalsIgnoreCase(row[0])
                            && row[1] != null
                            && row[1].toLowerCase().contains("unsigned");
                }
            }

            return false;
        } finally {
            closeResults(results);
        }
    }

    /**
     * Checks whether a MySQL table contains a matching foreign key.
     *
     * @param dao DAO used to execute the metadata query.
     * @param tableName Name of the table to inspect.
     * @param constraintName Name of the expected foreign key.
     * @param columnName Name of the local foreign key column.
     * @param parentTableName Name of the referenced table.
     * @param parentColumnName Name of the referenced column.
     * @return True when the foreign key exists.
     * @throws SQLException when the metadata query fails.
     */
    private boolean hasForeignKey(Dao<?, ?> dao, String tableName, String constraintName, String columnName, String parentTableName, String parentColumnName)
            throws SQLException {
        GenericRawResults<String[]> results = dao.queryRaw(
                "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +"WHERE TABLE_SCHEMA = DATABASE() " +"AND TABLE_NAME = ? " +"AND REFERENCED_TABLE_NAME IS NOT NULL " +"AND (CONSTRAINT_NAME = ? " +"OR (COLUMN_NAME = ? AND REFERENCED_TABLE_NAME = ? AND REFERENCED_COLUMN_NAME = ?))", tableName, constraintName, columnName, parentTableName, parentColumnName);

        try {
            return results.iterator().hasNext();
        } finally {
            closeResults(results);
        }
    }
}
