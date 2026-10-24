package me.bronsk.aethercore.ui.features.admintools;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.admintools.AEAdminToolsModule;
import me.bronsk.aethercore.modules.player.models.AEAdminSettings;
import me.bronsk.aethercore.ui.components.AECheckbox;
import me.bronsk.aethercore.ui.components.AEInputField;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * The "Admin Tools" tab - a personal page: every admin who opens it sees and toggles their OWN
 * vanish/gamemode/spectator/freecam/walk-speed/fly-speed, loaded fresh from {@link
 * AEAdminToolsModule#getSettings} at construction. Unlike the staged-then-Save shape of {@code
 * AEUIGroupDetailsView}/{@code AEUIChatGroupSettingsView}, every field here applies (and persists)
 * the moment it changes - no Save button, no draft state to lose across a sub-tab switch since
 * there isn't one. Walk speed and fly speed are two independent fields, not one - the native {@code
 * MovementSettings} tracks {@code baseSpeed} (walking) and {@code horizontalFlySpeed}/{@code
 * verticalFlySpeed} (flying, e.g. while in FreeCam or Spectator) as separate values.
 */
public final class AEUIAdminToolsView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/AdminTools/AEUIAdminToolsView.ui";

    private boolean vanish;
    private String gamemode;
    private boolean spectator;
    private boolean freeCam;
    private String speedText;
    private String flySpeedText;

    public AEUIAdminToolsView() {
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        loadIfNeeded();
        return Map.of(
                "Vanish", new AEInputField(lang("ui.admintools.vanish"), new AECheckbox(vanish, this::onVanishChanged)),
                "Gamemode", new AEInputField(lang("ui.admintools.gamemode"), new AEGamemodeSelector(gamemode, this::onGamemodeChanged)),
                "Spectator", new AEInputField(lang("ui.admintools.spectator"), new AECheckbox(spectator, this::onSpectatorChanged)),
                "FreeCam", new AEInputField(lang("ui.admintools.freecam"), new AECheckbox(freeCam, this::onFreeCamChanged)),
                "Speed", new AEInputField(lang("ui.admintools.speed"), new AETextInput("1", speedText, this::onSpeedChanged)),
                "FlySpeed", new AEInputField(lang("ui.admintools.fly_speed"), new AETextInput("1", flySpeedText, this::onFlySpeedChanged))
        );
    }

    private boolean loaded;

    private void loadIfNeeded() {
        if (loaded) {
            return;
        }
        loaded = true;
        AEAdminSettings settings = getAdminToolsModule().getSettings(getActorUuid());
        vanish = settings.isVanish();
        gamemode = settings.getGamemode();
        spectator = settings.isSpectator();
        freeCam = settings.isFreeCam();
        speedText = String.valueOf(settings.getSpeedLevel());
        flySpeedText = String.valueOf(settings.getFlySpeedLevel());
    }

    private void onVanishChanged(boolean value) {
        vanish = value;
        showNotification(getAdminToolsModule().handleSetVanish(getActorContext(), value));
        refresh("Vanish");
    }

    /** Switching gamemode exits Spectator server-side (see {@code AEAdminToolsModule#handleSetGamemode}) - mirror that locally so the checkbox reflects it without a second round trip. */
    private void onGamemodeChanged(String value) {
        gamemode = value;
        AEExecutionContext context = getActorContext();
        AEActionResult result = getAdminToolsModule().handleSetGamemode(context, value);
        boolean spectatorChanged = spectator;
        spectator = false;
        showNotification(result);
        refresh(spectatorChanged ? new String[]{"Gamemode", "Spectator"} : new String[]{"Gamemode"});
    }

    private void onSpectatorChanged(boolean value) {
        spectator = value;
        showNotification(getAdminToolsModule().handleSetSpectator(getActorContext(), value));
        refresh("Spectator");
    }

    private void onFreeCamChanged(boolean value) {
        freeCam = value;
        showNotification(getAdminToolsModule().handleSetFreeCam(getActorContext(), value));
        refresh("FreeCam");
    }

    private void onSpeedChanged(String value) {
        speedText = value;
        int speedLevel = parseSpeedLevel(value);
        showNotification(getAdminToolsModule().handleSetSpeed(getActorContext(), speedLevel));
    }

    private void onFlySpeedChanged(String value) {
        flySpeedText = value;
        int flySpeedLevel = parseSpeedLevel(value);
        showNotification(getAdminToolsModule().handleSetFlySpeed(getActorContext(), flySpeedLevel));
    }

    private static int parseSpeedLevel(String text) {
        try {
            return Math.max(1, Integer.parseInt(text.trim()));
        } catch (NumberFormatException | NullPointerException ignored) {
            return 1;
        }
    }

    private AEAdminToolsModule getAdminToolsModule() {
        return AEModulesManager.get().getModule(AEPlayersModule.class).getAdminToolsModule();
    }
}
