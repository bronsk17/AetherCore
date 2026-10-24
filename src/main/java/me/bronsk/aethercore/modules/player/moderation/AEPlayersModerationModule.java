package me.bronsk.aethercore.modules.player.moderation;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.DelegateItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEExecutionContextFactory;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayerPerms;
import me.bronsk.aethercore.modules.player.models.AEPlayerBan;
import me.bronsk.aethercore.modules.player.models.AEPlayerMute;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.commands.AEBanCommand;
import me.bronsk.aethercore.modules.player.commands.AEFreezeCommand;
import me.bronsk.aethercore.modules.player.commands.AEKickCommand;
import me.bronsk.aethercore.modules.player.commands.AEMuteCommand;
import me.bronsk.aethercore.modules.player.commands.AETempBanCommand;
import me.bronsk.aethercore.modules.player.commands.AETempMuteCommand;
import me.bronsk.aethercore.modules.player.commands.AEUnbanCommand;
import me.bronsk.aethercore.modules.player.commands.AEUnfreezeCommand;
import me.bronsk.aethercore.modules.player.commands.AEUnmuteCommand;
import me.bronsk.aethercore.modules.player.storage.AEPlayersStorage;
import me.bronsk.aethercore.modules.player.systems.AEPlayerFreezeChangeSystem;
import me.bronsk.aethercore.modules.player.systems.AEPlayerFreezeSystem;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

public class AEPlayersModerationModule extends AESubModule<AEPlayersModule, AEPlayersStorage> {

    @Override
    public String getName() {
        return "moderation";
    }

    @Override
    protected void onInit() {
        registerListener(new AEPlayersModerationListener(this));

        AEPlayerFreezeSystem freezeSystem = new AEPlayerFreezeSystem();
        AetherPlugin.getPlugin().getEntityStoreRegistry().registerSystem(freezeSystem);
        AetherPlugin.getPlugin().getEntityStoreRegistry().registerSystem(new AEPlayerFreezeChangeSystem(freezeSystem));

        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEBanCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AETempBanCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEMuteCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AETempMuteCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEUnbanCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEUnmuteCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEFreezeCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEUnfreezeCommand());
        AetherPlugin.getPlugin().getCommandRegistry().registerCommand(new AEKickCommand());
    }

    /** Whether {@code playerUuid} currently has an active ban - used by the Dupe IP lookup to color a row, and to toggle the Ban/Unban button. */
    public boolean isBanned(UUID playerUuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getActiveBan(playerUuid.toString())) != null;
    }

    /** Whether {@code playerUuid} currently has an active mute - used to toggle the Mute/Unmute button. */
    public boolean isMuted(UUID playerUuid) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getActiveMute(playerUuid.toString())) != null;
    }

    public AEActionResult handleBan(AEExecutionContext context, String player, String reason) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String normalizedReason = normalizeReason(reason);
        String playerUuidString = playerUuid.toString();
        AEPlayersStorage storage = getStorage();
        replaceActiveBan(storage, playerUuidString, context);

        AEDatabaseUtils.supplyUnchecked(
                () -> storage.createBan(playerUuidString, actorUuid(context), normalizedReason, null));
        kickPlayerByUuid(playerUuid, Message.raw(normalizedReason));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.ban.success", context, args("target", getTargetName(playerUuid), "reason", normalizedReason)));
    }

    public AEActionResult handleTempBan(AEExecutionContext context, String player, Duration duration, String reason) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String normalizedReason = normalizeReason(reason);
        String playerUuidString = playerUuid.toString();
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(duration));
        AEPlayersStorage storage = getStorage();
        replaceActiveBan(storage, playerUuidString, context);

        AEDatabaseUtils.supplyUnchecked(() -> storage.createBan(
                playerUuidString,
                actorUuid(context),
                normalizedReason,
                expiresAt));
        kickPlayerByUuid(playerUuid, Message.raw(normalizedReason));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.tempban.success", context, args("target", getTargetName(playerUuid), "reason", normalizedReason, "expires", formatExpiresAt(expiresAt))));
    }

    public AEActionResult handleUnban(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String playerUuidString = playerUuid.toString();
        AEPlayersStorage storage = getStorage();
        boolean revoked = AEDatabaseUtils.supplyUnchecked(
                () -> storage.revokeBan(playerUuidString, actorUuid(context), "unban"));
        if (!revoked) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.ban.missing", context, args("target", getTargetName(playerUuid))));
        }

        return AEActionResult.success(getLangModule().buildMessage("player.moderation.unban.success", context, args("target", getTargetName(playerUuid))));
    }

    public AEActionResult handleMute(AEExecutionContext context, String player, String reason) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String normalizedReason = normalizeReason(reason);
        String playerUuidString = playerUuid.toString();
        AEPlayersStorage storage = getStorage();
        replaceActiveMute(storage, playerUuidString, context);

        AEDatabaseUtils.supplyUnchecked(() -> storage.createMute(
                playerUuidString,
                actorUuid(context),
                normalizedReason,
                null));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.mute.success", context, args("target", getTargetName(playerUuid), "reason", normalizedReason)));
    }

    public AEActionResult handleTempMute(AEExecutionContext context, String player, Duration duration, String reason) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String normalizedReason = normalizeReason(reason);
        String playerUuidString = playerUuid.toString();
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(duration));
        AEPlayersStorage storage = getStorage();
        replaceActiveMute(storage, playerUuidString, context);

        AEDatabaseUtils.supplyUnchecked(() -> storage.createMute(
                playerUuidString,
                actorUuid(context),
                normalizedReason,
                expiresAt));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.tempmute.success", context, args("target", getTargetName(playerUuid), "reason", normalizedReason, "expires", formatExpiresAt(expiresAt))));
    }

    public AEActionResult handleUnmute(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String playerUuidString = playerUuid.toString();
        AEPlayersStorage storage = getStorage();
        boolean revoked = AEDatabaseUtils.supplyUnchecked(
                () -> storage.revokeMute(playerUuidString, actorUuid(context), "unmute"));
        if (!revoked) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.mute.missing", context, args("target", getTargetName(playerUuid))));
        }

        return AEActionResult.success(getLangModule().buildMessage("player.moderation.unmute.success", context, args("target", getTargetName(playerUuid))));
    }

    /** Revokes whatever ban is currently active for {@code playerUuidString}, if any - lets a fresh Ban replace it instead of erroring. */
    private void replaceActiveBan(AEPlayersStorage storage, String playerUuidString, AEExecutionContext context) {
        if (AEDatabaseUtils.supplyUnchecked(() -> storage.getActiveBan(playerUuidString)) != null) {
            AEDatabaseUtils.runUnchecked(() -> storage.revokeBan(playerUuidString, actorUuid(context), "replaced"));
        }
    }

    /** Revokes whatever mute is currently active for {@code playerUuidString}, if any - lets a fresh Mute replace it instead of erroring. */
    private void replaceActiveMute(AEPlayersStorage storage, String playerUuidString, AEExecutionContext context) {
        if (AEDatabaseUtils.supplyUnchecked(() -> storage.getActiveMute(playerUuidString)) != null) {
            AEDatabaseUtils.runUnchecked(() -> storage.revokeMute(playerUuidString, actorUuid(context), "replaced"));
        }
    }

    public Message handlePlayerSetupConnect(AEExecutionContext context, UUID playerUuid) {
        AEPlayerBan ban = AEDatabaseUtils.supplyUnchecked(
                () -> getStorage().getActiveBan(playerUuid.toString()));
        if (ban == null) {
            return null;
        }

        return getLangModule().buildMessage("player.moderation.ban.disconnect", context, args("reason", ban.getReason(), "expires", formatExpiresAt(ban.getExpiresAt())));
    }

    public Message handlePlayerChat(AEExecutionContext context, UUID playerUuid) {
        AEPlayerMute mute = AEDatabaseUtils.supplyUnchecked(
                () -> getStorage().getActiveMute(playerUuid.toString()));
        if (mute == null) {
            return null;
        }

        return getLangModule().buildMessage("player.moderation.mute.chat", context, args("reason", mute.getReason(), "expires", formatExpiresAt(mute.getExpiresAt())));
    }

    private Message missingPlayerMessage(AEExecutionContext context, String player) {
        return getLangModule().buildMessage("player.moderation.player.missing", context, args("target", player));
    }

    private String actorUuid(AEExecutionContext context) {
        if (context == null || context.actorUuid() == null) {
            return null;
        }
        return context.actorUuid().toString();
    }

    private String getTargetName(UUID playerUuid) {
        String name = getParentModule().getPlayerName(playerUuid);
        return name == null ? playerUuid.toString() : name;
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "No reason specified";
        }
        return reason.trim();
    }

    private String formatExpiresAt(Timestamp expiresAt) {
        return expiresAt == null ? "never" : expiresAt.toString();
    }

    private void kickPlayerByUuid(UUID playerUuid, Message reason) {
        final AEGamePlayer tcGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        if (tcGamePlayer != null) {
            tcGamePlayer.kick(reason);
        }
    }

    public AEActionResult handleFreeze(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        AEGamePlayer tcGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        String target = getTargetName(playerUuid);
        if (tcGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.freeze.offline", context, args("target", target)));
        }
        tcGamePlayer.freeze();
        AEExecutionContext targetContext = AEExecutionContextFactory.get().fromUuid(playerUuid);
        tcGamePlayer.sendMessage(getLangModule().buildMessage("player.moderation.freeze.frozen", targetContext));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.freeze.success", context, args("target", target)));
    }

    public AEActionResult handleUnfreeze(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        AEGamePlayer tcGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        String target = getTargetName(playerUuid);
        if (tcGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.freeze.offline", context, args("target", target)));
        }

        tcGamePlayer.unfreeze();
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.unfreeze.success", context, args("target", target)));
    }

    public AEActionResult handleKick(AEExecutionContext context, String player, String reason) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String target = getTargetName(playerUuid);
        AEGamePlayer tcGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        if (tcGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.kick.offline", context, args("target", target)));
        }

        String normalizedReason = normalizeReason(reason);
        kickPlayerByUuid(playerUuid, Message.raw(normalizedReason));
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.kick.success", context, args("target", target, "reason", normalizedReason)));
    }

    /** Teleports the actor to {@code player}'s current position. */
    public AEActionResult handleGoTo(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String target = getTargetName(playerUuid);
        AEGamePlayer targetGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        if (targetGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.goto.offline", context, args("target", target)));
        }
        AEGamePlayer actorGamePlayer = AEGamePlayer.fromUuid(context.actorUuid());
        if (actorGamePlayer == null) {
            return AEActionResult.error(null);
        }

        actorGamePlayer.teleport(targetGamePlayer.getWorld(), targetGamePlayer.getTransform());
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.goto.success", context, args("target", target)));
    }

    /** Teleports {@code player} to the actor's current position. */
    public AEActionResult handleBring(AEExecutionContext context, String player) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String target = getTargetName(playerUuid);
        AEGamePlayer targetGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        if (targetGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.bring.offline", context, args("target", target)));
        }
        AEGamePlayer actorGamePlayer = AEGamePlayer.fromUuid(context.actorUuid());
        if (actorGamePlayer == null) {
            return AEActionResult.error(null);
        }

        targetGamePlayer.teleport(actorGamePlayer.getWorld(), actorGamePlayer.getTransform());
        return AEActionResult.success(getLangModule().buildMessage("player.moderation.bring.success", context, args("target", target)));
    }

    /**
     * Opens {@code player}'s inventory as a container window on the actor's own client - the same
     * {@link InventoryComponent#HOTBAR_FIRST} layout and {@link ContainerWindow} the native
     * {@code /inv see} command uses. Read-only (every slot denied) unless the actor holds {@link
     * AEPlayerPerms#INVENTORY_MODIFY}. {@code onClosed}, when given, runs once the actor closes the
     * window - the caller uses it to bring the admin panel back to the player's Overview tab.
     */
    public AEActionResult handleOpenInventory(AEExecutionContext context, String player, Runnable onClosed) {
        UUID playerUuid = getParentModule().resolvePlayerUuid(player);
        if (playerUuid == null) {
            return AEActionResult.error(missingPlayerMessage(context, player));
        }
        String target = getTargetName(playerUuid);
        AEGamePlayer targetGamePlayer = AEGamePlayer.fromUuid(playerUuid);
        if (targetGamePlayer == null) {
            return AEActionResult.error(getLangModule().buildMessage("player.moderation.inventory.offline", context, args("target", target)));
        }
        AEGamePlayer actorGamePlayer = AEGamePlayer.fromUuid(context.actorUuid());
        if (actorGamePlayer == null) {
            return AEActionResult.error(null);
        }

        boolean canModify = actorGamePlayer.hasPermission(AEPlayerPerms.INVENTORY_MODIFY);
        actorGamePlayer.executeOnWorld(() -> {
            CombinedItemContainer combined = InventoryComponent.getCombined(
                    targetGamePlayer.getStore(), targetGamePlayer.getEntityStoreRef(), InventoryComponent.HOTBAR_FIRST);
            ItemContainer container = combined;
            if (!canModify) {
                DelegateItemContainer<CombinedItemContainer> readOnly = new DelegateItemContainer<>(combined);
                readOnly.setGlobalFilter(FilterType.DENY_ALL);
                container = readOnly;
            }
            ContainerWindow window = new ContainerWindow(container);
            if (onClosed != null) {
                window.registerCloseEvent(event -> actorGamePlayer.executeOnWorld(onClosed));
            }
            actorGamePlayer.getPlayer().getPageManager()
                    .setPageWithWindows(actorGamePlayer.getEntityStoreRef(), actorGamePlayer.getStore(), Page.Bench, true, window);
        });
        return AEActionResult.success(null);
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
