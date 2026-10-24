package me.bronsk.aethercore.ui.features.players;

import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.ui.avatar.AEDynamicImageAsset;
import me.bronsk.aethercore.ui.avatar.AEHyvatarClient;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEMetricCard;
import me.bronsk.aethercore.ui.components.AEPlayerTag;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.utils.AEDateUtils;
import org.joml.Vector3i;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Player Details' Overview sub-tab: avatar, identity rows, and the non-punitive utility actions. The
 * avatar starts as the player's initial letter; a hyvatar.io full-body render is fetched
 * asynchronously in the background and swapped in on success only (see {@link
 * #fetchAvatarIfNeeded()}) - a slow or failed render never blocks or breaks the rest of the tab.
 */
final class AEUIPlayerOverviewView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEUIPlayerOverviewView.ui";
    private static final int AVATAR_RENDER_SIZE = 512;
    private static final String AVATAR_ASSET_NAME_PREFIX = "UI/Custom/Pages/AetherCore/Elements/DynamicAvatar/avatar";

    private final AEPlayer player;
    private volatile boolean avatarFetchStarted;
    private volatile String avatarAssetPath;
    private volatile boolean detached;

    AEUIPlayerOverviewView(AEPlayer player) {
        this.player = player;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        fetchAvatarIfNeeded();
        return Map.of("Details", new AEPlayerDetails(getAvatarInitial(), avatarAssetPath, getDetailRows(), getActionButtons(), getCards()));
    }

    @Override
    protected void onDetach() {
        detached = true;
    }

    /**
     * Kicks off the hyvatar.io download exactly once per instance (guarded, since {@link
     * #getContent()} can be called again by an unrelated refresh) - on success, registers the render
     * as a common asset (see {@link AEDynamicImageAsset#register}) and swaps the avatar in; on
     * failure or timeout, does nothing and the initial-letter avatar stays. Never touches the UI from
     * the async callback if this view has already been navigated away from (see {@link #onDetach()})
     * - refreshing a selector that no longer exists disconnects the player.
     */
    private void fetchAvatarIfNeeded() {
        String username = player.getName();
        if (avatarFetchStarted || username == null || username.isBlank()) {
            return;
        }
        avatarFetchStarted = true;
        AELogger.logInfo("[avatar] starting download for username=%s", username);
        AEHyvatarClient.downloadFullRender(username, AVATAR_RENDER_SIZE).thenAccept(pngBytes -> {
            if (pngBytes == null) {
                AELogger.logInfo("[avatar] download failed/rejected for %s", username);
                return;
            }
            AELogger.logInfo("[avatar] downloaded %d bytes for %s, detached=%s", pngBytes.length, username, detached);
            if (detached) {
                return;
            }
            AEDynamicImageAsset asset = AEDynamicImageAsset.register(AVATAR_ASSET_NAME_PREFIX, pngBytes);
            AELogger.logInfo("[avatar] registered common asset name=%s hash=%s", asset.getName(), asset.getHash());
            UUID adminUuid = getActorUuid();
            AEGamePlayer admin = AEGamePlayer.fromUuid(adminUuid);
            if (admin == null) {
                AELogger.logInfo("[avatar] admin %s not resolvable, skipping push", adminUuid);
                return;
            }
            try {
                asset.pushTo(admin.getPlayerRef().getPacketHandler());
                AELogger.logInfo("[avatar] pushTo completed without throwing");
            } catch (Exception exception) {
                AELogger.logException("[avatar] pushTo threw", exception);
                return;
            }
            try {
                runScheduledUpdate(() -> {
                    avatarAssetPath = asset.getName();
                    refresh("Details");
                });
                AELogger.logInfo("[avatar] refresh(Details) completed without throwing");
            } catch (Exception exception) {
                AELogger.logException("[avatar] refresh(Details) threw", exception);
            }
        }).exceptionally(throwable -> {
            AELogger.logException("[avatar] download future itself failed", throwable);
            return null;
        });
    }

    private String getAvatarInitial() {
        return AEUIPlayerDetailsView.getDisplayName(player).substring(0, 1).toUpperCase();
    }

    private List<AEPlayerDetails.Row> getDetailRows() {
        return List.of(
                new AEPlayerDetails.Row(lang("ui.player.field.uuid"), player.getUuid()),
                new AEPlayerDetails.Row(lang("ui.player.field.created_at"), AEDateUtils.formatByConfig(player.getCreatedAt())),
                new AEPlayerDetails.Row(lang("ui.player.field.updated_at"), AEDateUtils.formatByConfig(player.getUpdatedAt())),
                new AEPlayerDetails.Row(lang("ui.player.field.last_ip"), player.getLastIp() == null ? "-" : player.getLastIp()),
                new AEPlayerDetails.Row(lang("ui.player.field.groups"), formatGroups(player.getUuid()))
        );
    }

    private static String formatGroups(String playerUuid) {
        Set<String> groups = AEModulesManager.get().getModule(AEPermissionsModule.class).getUsersModule().getGroupsForUser(UUID.fromString(playerUuid));
        return groups.isEmpty() ? "-" : String.join(", ", groups);
    }

    private List<AEUIComponent> getActionButtons() {
        return List.of(
                new AEActionButton(lang("ui.player.action.goto"), this::doGoTo),
                new AEActionButton(lang("ui.player.action.bring"), this::doBring),
                new AEActionButton(lang("ui.player.action.inventory"), this::openInventory),
                new AEActionButton(lang("ui.player.action.dupe_ip"), this::openDupeIp)
        );
    }

    /** World/Position and Health/Stamina cards - both need the player's live entity, so both simply show placeholders while the player is offline. */
    private List<AEUIComponent> getCards() {
        AEGamePlayer gamePlayer = AEGamePlayer.fromUuid(player.getUuid());
        return List.of(worldCard(gamePlayer), vitalsCard(gamePlayer));
    }

    private AEUIComponent worldCard(AEGamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return new AEMetricCard(lang("ui.player.offline"), lang("ui.player.card.world_position"));
        }
        World world = gamePlayer.getWorld();
        Vector3i block = gamePlayer.getBlockIsStandingOn();
        String position = block.x + ", " + block.y + ", " + block.z;
        return new AEMetricCard(world.getName(), position);
    }

    private AEUIComponent vitalsCard(AEGamePlayer gamePlayer) {
        String healthLabel = lang("ui.player.health");
        String staminaLabel = lang("ui.player.stamina");
        if (gamePlayer == null) {
            return new AEPlayerVitalsCard(vitalsText(healthLabel, null), 0, vitalsText(staminaLabel, null), 0);
        }
        EntityStatMap stats = gamePlayer.getComponent(EntityStatMap.getComponentType());
        if (stats == null) {
            return new AEPlayerVitalsCard(vitalsText(healthLabel, null), 0, vitalsText(staminaLabel, null), 0);
        }
        EntityStatValue health = stats.get(DefaultEntityStatTypes.getHealth());
        EntityStatValue stamina = stats.get(DefaultEntityStatTypes.getStamina());
        return new AEPlayerVitalsCard(
                vitalsText(healthLabel, health), health == null ? 0 : health.asPercentage(),
                vitalsText(staminaLabel, stamina), stamina == null ? 0 : stamina.asPercentage());
    }

    private static String vitalsText(String label, EntityStatValue value) {
        if (value == null) {
            return label + " -";
        }
        return label + " " + Math.round(value.get()) + "/" + Math.round(value.getMax());
    }

    private void doGoTo() {
        showNotification(getModerationModule().handleGoTo(getActorContext(), player.getUuid()));
    }

    private void doBring() {
        showNotification(getModerationModule().handleBring(getActorContext(), player.getUuid()));
    }

    /** Opens the player's inventory as a native container window on the admin's own client; closing it brings the admin panel back here (Overview), not wherever the admin was before. */
    private void openInventory() {
        AEActionResult result = getModerationModule().handleOpenInventory(getActorContext(), player.getUuid(), this::reopenOverview);
        showNotification(result);
    }

    private void reopenOverview() {
        navigate(new AEUIPlayerDetailsView(player));
    }

    private void openDupeIp() {
        String ip = player.getLastIp();
        List<AEPlayer> matches = ip == null
                ? List.of()
                : AEModulesManager.get().getModule(AEPlayersModule.class).getPlayersByIp(ip).stream()
                        .filter(candidate -> !candidate.getUuid().equals(player.getUuid()))
                        .toList();
        showPopup(new AEUIDupeIpPopup(ip == null ? "-" : ip, matches, this::resolveDupeStatus, this::openDupePlayer));
    }

    private AEPlayerTag.Variant resolveDupeStatus(AEPlayer candidate) {
        if (getModerationModule().isBanned(UUID.fromString(candidate.getUuid()))) {
            return AEPlayerTag.Variant.DANGER;
        }
        return AEUIPlayerDetailsView.isOnline(candidate) ? AEPlayerTag.Variant.SUCCESS : AEPlayerTag.Variant.MUTED;
    }

    private void openDupePlayer(AEPlayer target) {
        closePopup();
        navigate(new AEUIPlayerDetailsView(target));
    }

    private AEPlayersModerationModule getModerationModule() {
        return AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
    }
}
