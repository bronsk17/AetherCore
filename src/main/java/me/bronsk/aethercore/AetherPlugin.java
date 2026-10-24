package me.bronsk.aethercore;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.chat.AEChatModule;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.ui.commands.AERootCommand;
import me.bronsk.aethercore.ui.framework.AEUIManager;
import me.bronsk.aethercore.utils.AEConsoleLogBuffer;
import me.bronsk.aethercore.utils.AEScheduler;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class AetherPlugin extends JavaPlugin {

    private static AetherPlugin plugin;

    private AEModulesManager modulesManager;
    private AEUIManager uiManager;
    private AERootCommand rootCommand;

    /**
     * Creates a new instance.
     *
     * @param init Method argument.
     */
    public AetherPlugin(@NonNullDecl JavaPluginInit init) {
        super(init);
        plugin = this;
    }

    /**
     * Sets setup.
     */
    @Override
    protected void setup() {
        setupUI();
        setupModules();
        getCommandRegistry().registerCommand(rootCommand);
        AEConsoleLogBuffer.start();
    }

    /**
     * Handles shutdown.
     */
    @Override
    protected void shutdown() {
        AEConsoleLogBuffer.stop();
        AEScheduler.shutdown();

        if (modulesManager == null) {
            return;
        }

        try {
            modulesManager.closeModules();
        } catch (Exception e) {
            AELogger.logException("Failed to close modules", e);
        }
    }

    /**
     * Loads config and core modules.
     */
    private void setupModules() {
        modulesManager = AEModulesManager.get();
        modulesManager.clear();

        try {
            modulesManager.registerModule(new AELangModule());
            modulesManager.registerModule(new AEPlayersModule());
            modulesManager.registerModule(new AEPermissionsModule());
            modulesManager.registerModule(new AEChatModule());

            modulesManager.initModules();
            modulesManager.getDatabaseInstaller().run();
            modulesManager.getModule(AELangModule.class).reloadMessages();
        } catch (Exception exception) {
            AELogger.logException("Failed to setup modules", exception);
            throw new IllegalStateException("Failed to setup modules", exception);
        }
    }

    /**
     * Initializes the shared AetherCore user interface entry point and the {@code /aether} root
     * command - created here, before modules initialize, so each module's {@code onInit()} can add
     * its own command as a subcommand via {@link #getRootCommand()}. Registered with the server only
     * after {@link #setupModules()} has run, once every subcommand has actually been added.
     */
    private void setupUI() {
        uiManager = new AEUIManager();
        rootCommand = new AERootCommand(uiManager);
    }

    /**
     * Returns getPlugin.
     *
     * @return Method result.
     */
    public static AetherPlugin getPlugin() {
        return plugin;
    }

    /**
     * The shared {@code /aether} root command - every other module's own command attaches to it as a
     * subcommand from within its own {@code onInit()} instead of registering itself directly.
     *
     * @return Root command.
     */
    public AERootCommand getRootCommand() {
        return rootCommand;
    }

}
