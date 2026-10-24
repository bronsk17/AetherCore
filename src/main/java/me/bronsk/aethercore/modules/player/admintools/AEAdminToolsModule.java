package me.bronsk.aethercore.modules.player.admintools;

import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEAdminSettings;
import me.bronsk.aethercore.modules.player.storage.AEPlayersStorage;
import me.bronsk.aethercore.modules.player.systems.AEPlayerVanishedSystem;

import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * A personal admin toolbox - every handler acts on the ACTOR themselves (self-service, no target
 * argument), unlike {@code AEPlayersModerationModule} which always acts on someone else. Settings
 * are persisted in {@code aeadminsettings} (a table on the shared {@link AEPlayersStorage}, same as
 * how bans/mutes live there rather than owning a separate storage) and reapplied on every join via
 * {@link #applyOnJoin} - see {@code AEPlayersModule#handleJoin}.
 */
public class AEAdminToolsModule extends AESubModule<AEPlayersModule, AEPlayersStorage> {

    @Override
    public String getName() {
        return "admintools";
    }

    @Override
    protected void onInit() {
        AetherPlugin.getPlugin().getEntityStoreRegistry().registerSystem(new AEPlayerVanishedSystem());
    }

    /** The actor's own settings, defaulted (not yet persisted) if this is their first time. */
    public AEAdminSettings getSettings(UUID uuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getOrCreateAdminSettings(uuid.toString()));
    }

    public AEActionResult handleSetVanish(AEExecutionContext context, boolean vanish) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        actor.setIsVanished(vanish);
        AEAdminSettings settings = getSettings(uuid);
        settings.setVanish(vanish);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        String code = vanish ? "admintools.vanish.enabled" : "admintools.vanish.disabled";
        return AEActionResult.success(getLangModule().buildMessage(code, context));
    }

    /** {@code gamemode} is "Adventure" or "Creative" - switching away from Spectator first if it was active, since the two are mutually exclusive. */
    public AEActionResult handleSetGamemode(AEExecutionContext context, String gamemode) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        AEAdminSettings settings = getSettings(uuid);
        if (settings.isSpectator()) {
            actor.exitSpectator();
            settings.setSpectator(false);
        }
        actor.setGamemode(gamemode);
        settings.setGamemode(gamemode);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        return AEActionResult.success(getLangModule().buildMessage("admintools.gamemode.changed", context, args("gamemode", gamemode)));
    }

    public AEActionResult handleSetSpectator(AEExecutionContext context, boolean spectator) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        boolean applied = spectator ? actor.enterSpectator() : actor.exitSpectator();
        if (!applied) {
            return AEActionResult.error(getLangModule().buildMessage("admintools.spectator.failed", context));
        }
        AEAdminSettings settings = getSettings(uuid);
        settings.setSpectator(spectator);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        String code = spectator ? "admintools.spectator.enabled" : "admintools.spectator.disabled";
        return AEActionResult.success(getLangModule().buildMessage(code, context));
    }

    public AEActionResult handleSetFreeCam(AEExecutionContext context, boolean freeCam) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        actor.setFreeCam(freeCam);
        AEAdminSettings settings = getSettings(uuid);
        settings.setFreeCam(freeCam);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        String code = freeCam ? "admintools.freecam.enabled" : "admintools.freecam.disabled";
        return AEActionResult.success(getLangModule().buildMessage(code, context));
    }

    public AEActionResult handleSetSpeed(AEExecutionContext context, int level) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        int normalized = Math.max(1, level);
        actor.setSpeedLevel(normalized);
        AEAdminSettings settings = getSettings(uuid);
        settings.setSpeedLevel(normalized);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        return AEActionResult.success(getLangModule().buildMessage("admintools.speed.changed", context, args("level", String.valueOf(normalized))));
    }

    public AEActionResult handleSetFlySpeed(AEExecutionContext context, int level) {
        UUID uuid = context.actorUuid();
        AEGamePlayer actor = AEGamePlayer.fromUuid(uuid);
        if (actor == null) {
            return AEActionResult.error(offlineMessage(context));
        }
        int normalized = Math.max(1, level);
        actor.setFlySpeedLevel(normalized);
        AEAdminSettings settings = getSettings(uuid);
        settings.setFlySpeedLevel(normalized);
        AEDatabaseUtils.runUnchecked(() -> getStorage().saveAdminSettings(settings));
        return AEActionResult.success(getLangModule().buildMessage("admintools.flyspeed.changed", context, args("level", String.valueOf(normalized))));
    }

    /** Reapplies every persisted setting to a just-joined player - called from {@code AEPlayersModule#handleJoin}. */
    public void applyOnJoin(UUID uuid) {
        AEGamePlayer gamePlayer = AEGamePlayer.fromUuid(uuid);
        if (gamePlayer == null) {
            return;
        }
        AEAdminSettings settings = getSettings(uuid);
        if (settings.isVanish()) {
            gamePlayer.setIsVanished(true);
        }
        gamePlayer.setGamemode(settings.getGamemode());
        if (settings.isSpectator()) {
            gamePlayer.enterSpectator();
        }
        gamePlayer.setFreeCam(settings.isFreeCam());
        gamePlayer.setSpeedLevel(settings.getSpeedLevel());
        gamePlayer.setFlySpeedLevel(settings.getFlySpeedLevel());
    }

    private com.hypixel.hytale.server.core.Message offlineMessage(AEExecutionContext context) {
        return getLangModule().buildMessage("admintools.actor.offline", context);
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
