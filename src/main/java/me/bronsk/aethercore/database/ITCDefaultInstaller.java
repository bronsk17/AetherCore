package me.bronsk.aethercore.database;

import java.sql.SQLException;

@FunctionalInterface
public interface ITCDefaultInstaller {

    /**
     * Installs default persistent data.
     *
     * @throws SQLException when default data installation fails.
     */
    void install() throws SQLException;

}
