package me.bronsk.aethercore.database;

import java.sql.SQLException;

public final class AEDatabaseUtils {

    /**
     * Prevents utility class instantiation.
     */
    private AEDatabaseUtils() {
    }

    /**
     * Runs a named SQL operation and wraps SQL exceptions.
     *
     * @param runnable SQL operation to run.
     */
    public static void runUnchecked(SqlRunnable runnable) {
        try {
            runnable.run();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Runs a named SQL supplier and wraps SQL exceptions.
     *
     * @param supplier SQL supplier to run.
     * @return Supplier result.
     * @param <T> Supplier result type.
     */
    public static <T> T supplyUnchecked(SqlSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (SQLException e) {
            throw new IllegalStateException("Error while supplying from database", e);
        }
    }

    @FunctionalInterface
    public interface SqlRunnable {

        /**
         * Runs a SQL operation.
         *
         * @throws SQLException when the SQL operation fails.
         */
        void run() throws SQLException;
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {

        /**
         * Gets a SQL-backed value.
         *
         * @return SQL-backed value.
         * @throws SQLException when the SQL operation fails.
         */
        T get() throws SQLException;
    }
}
