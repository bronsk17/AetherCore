package me.bronsk.aethercore.modules;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import me.bronsk.aethercore.modules.player.AEPlayersModule;

import java.util.UUID;

public final class AEExecutionContextFactory {

    private static AEExecutionContextFactory instance;

    private AEExecutionContextFactory() {}

    /**
     * Returns the initialized context factory.
     *
     * @return Context factory.
     */
    public static AEExecutionContextFactory get() {
        if (instance == null) {
            instance = new AEExecutionContextFactory();
        }
        return instance;
    }

    /**
     * Builds a player execution context from a UUID.
     *
     * @param uuid Actor UUID.
     * @return Player execution context.
     */
    public AEExecutionContext fromUuid(UUID uuid) {
        if (uuid == null) {
            return fromConsole();
        }
        String actorName = resolveActorName(uuid);
        String lang = resolveLang(uuid);
        return AEExecutionContext.player(uuid, actorName, lang);
    }

    /**
     * Builds a console execution context.
     *
     * @return Console execution context.
     */
    public AEExecutionContext fromConsole() {
        return AEExecutionContext.console(resolveDefaultLang());
    }

    /**
     * Builds a system execution context.
     *
     * @return System execution context.
     */
    public AEExecutionContext fromSystem() {
        return new AEExecutionContext(null, "system", resolveDefaultLang(), false);
    }

    private String resolveActorName(UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef != null && playerRef.getUsername() != null && !playerRef.getUsername().isBlank()) {
            return playerRef.getUsername();
        }

        AEModulesManager modulesManager = AEModulesManager.get();
        if (modulesManager.hasModule(AEPlayersModule.class)) {
            String storedName = modulesManager.getModule(AEPlayersModule.class).getPlayerName(uuid);
            if (storedName != null && !storedName.isBlank()) {
                return storedName;
            }
        }

        return uuid.toString();
    }

    private String resolveLang(UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef != null && playerRef.getLanguage() != null && !playerRef.getLanguage().isBlank()) {
            return playerRef.getLanguage();
        }
        return resolveDefaultLang();
    }

    private String resolveDefaultLang() {
        return AEModulesManager.get().getGlobalConfig().getPluginLang();
    }
}
