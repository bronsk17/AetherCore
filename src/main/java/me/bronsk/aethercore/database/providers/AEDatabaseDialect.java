package me.bronsk.aethercore.database.providers;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public interface AEDatabaseDialect {

    /**
     * Ensures that a table has the requested column.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table to inspect.
     * @param columnName Name of the column to ensure.
     * @param columnDefinition SQL definition used when the column is missing.
     * @throws SQLException when the schema query fails.
     */
    void ensureColumn(Dao<?, ?> dao, String tableName, String columnName, String columnDefinition)
            throws SQLException;

    /**
     * Ensures that a table column exists as an unsigned long bitmask.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table to inspect.
     * @param columnName Name of the column to ensure.
     * @throws SQLException when the schema query fails.
     */
    void ensureUnsignedLongColumn(Dao<?, ?> dao, String tableName, String columnName) throws SQLException;

    /**
     * Ensures that a table has a restrict foreign key.
     *
     * @param dao DAO used to execute raw schema queries.
     * @param tableName Name of the table receiving the foreign key.
     * @param constraintName Name of the foreign key constraint.
     * @param columnName Name of the local foreign key column.
     * @param parentTableName Name of the referenced table.
     * @param parentColumnName Name of the referenced column.
     * @throws SQLException when the schema query fails.
     */
    void ensureRestrictForeignKey(Dao<?, ?> dao, String tableName, String constraintName,
                                  String columnName, String parentTableName, String parentColumnName)
            throws SQLException;
}
