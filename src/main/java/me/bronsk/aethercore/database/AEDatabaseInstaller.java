package me.bronsk.aethercore.database;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AEDatabaseInstaller {

    private final Map<Class<? extends ITCDefaultInstaller>, ITCDefaultInstaller> installers =
            new LinkedHashMap<>();
    private State state = State.OPEN;

    /**
     * Registers a default data installer.
     *
     * @param installer Installer instance.
     */
    public void registerInstaller(ITCDefaultInstaller installer) {
        Objects.requireNonNull(installer, "installer");
        if (state != State.OPEN) {
            throw new IllegalStateException("Cannot register database installers after execution has started");
        }

        @SuppressWarnings("unchecked")
        Class<? extends ITCDefaultInstaller> installerClass =
                (Class<? extends ITCDefaultInstaller>) installer.getClass();
        if (installers.putIfAbsent(installerClass, installer) != null) {
            throw new IllegalArgumentException("Database installer " + installerClass.getSimpleName()
                    + " is already registered");
        }
    }

    /**
     * Runs every registered default data installer.
     *
     * @throws SQLException when an installer fails.
     */
    public void run() throws SQLException {
        if (state == State.COMPLETED) {
            throw new IllegalStateException("Database installers have already been executed");
        }
        if (state == State.RUNNING) {
            throw new IllegalStateException("Database installers are already running");
        }

        state = State.RUNNING;
        try {
            for (ITCDefaultInstaller installer : List.copyOf(installers.values())) {
                installer.install();
            }
            state = State.COMPLETED;
        } catch (SQLException | RuntimeException exception) {
            state = State.OPEN;
            throw exception;
        }
    }

    /**
     * Clears registered installers and restores the registry to its initial state.
     */
    public void clear() {
        if (state == State.RUNNING) {
            throw new IllegalStateException("Cannot clear database installers while they are running");
        }
        installers.clear();
        state = State.OPEN;
    }

    private enum State {
        OPEN,
        RUNNING,
        COMPLETED
    }
}
