package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEModerationHistoryQuery;
import me.bronsk.aethercore.modules.player.models.AEModerationHistoryResponse;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.modules.player.models.AEPlayerModerationHistoryEntry;
import me.bronsk.aethercore.modules.player.moderation.AEPlayersModerationModule;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEButtonBar;
import me.bronsk.aethercore.ui.components.AETable;
import me.bronsk.aethercore.ui.components.AEUIColDef;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/** Player Details' Moderation sub-tab: the punitive action buttons and the moderation history table. */
final class AEUIPlayerModerationView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEUIPlayerModerationView.ui";

    private final AEPlayer player;

    AEUIPlayerModerationView(AEPlayer player) {
        this.player = player;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Buttons", new AEButtonBar(getActionButtons()),
                "Table", new AETable<>("", getModerationColumns(), lang("ui.player.moderation.empty"),
                        query -> {
                            AEModerationHistoryResponse response = AEModulesManager.get().getModule(AEPlayersModule.class)
                                    .getModerationHistory(new AEModerationHistoryQuery(player.getUuid(), query.pageIndex(),
                                            query.pageSize(), query.search()));
                            return new AEPageResult<>(query, response.entries(), response.totalEntries());
                        },
                        this::openEntryDetails, true)
        );
    }

    private List<AEUIColDef<AEPlayerModerationHistoryEntry>> getModerationColumns() {
        return List.of(
                new AEUIColDef<>(lang("ui.player.moderation.col.type"), AEPlayerModerationHistoryEntry::type),
                new AEUIColDef<>(lang("ui.player.moderation.col.status"), AEPlayerModerationHistoryEntry::status),
                new AEUIColDef<>(lang("ui.player.moderation.col.reason"), AEPlayerModerationHistoryEntry::reason),
                new AEUIColDef<>(lang("ui.player.moderation.col.by"), AEPlayerModerationHistoryEntry::by),
                new AEUIColDef<>(lang("ui.player.moderation.col.revoked_by"), AEPlayerModerationHistoryEntry::revokedBy),
                new AEUIColDef<>(lang("ui.player.moderation.col.created_at"), entry -> AEDateUtils.formatByConfig(entry.createdAt()), Comparator.comparing(AEPlayerModerationHistoryEntry::createdAt)),
                new AEUIColDef<>(lang("ui.player.moderation.col.expires_at"), entry -> entry.expiresAt() == null ? lang("ui.common.never") : AEDateUtils.formatByConfig(entry.expiresAt()),
                        Comparator.comparing(AEPlayerModerationHistoryEntry::expiresAt, Comparator.nullsFirst(Comparator.naturalOrder())))
        );
    }

    private List<AEUIComponent> getActionButtons() {
        UUID uuid = UUID.fromString(player.getUuid());
        AEPlayersModerationModule moderation = getModerationModule();
        boolean banned = moderation.isBanned(uuid);
        boolean muted = moderation.isMuted(uuid);
        boolean frozen = isFrozen();
        return List.of(
                banned
                        ? new AEActionButton(lang("ui.player.moderation.unban"), AEActionButton.Variant.PRIMARY, this::revokeBan)
                        : new AEActionButton(lang("ui.player.moderation.ban"), AEActionButton.Variant.DANGER, this::confirmBan),
                muted
                        ? new AEActionButton(lang("ui.player.moderation.unmute"), AEActionButton.Variant.PRIMARY, this::revokeMute)
                        : new AEActionButton(lang("ui.player.moderation.mute"), AEActionButton.Variant.DANGER, this::confirmMute),
                new AEActionButton(lang("ui.player.moderation.kick"), AEActionButton.Variant.DANGER, this::doKick),
                frozen
                        ? new AEActionButton(lang("ui.player.moderation.unfreeze"), AEActionButton.Variant.PRIMARY, this::doUnfreeze)
                        : new AEActionButton(lang("ui.player.moderation.freeze"), AEActionButton.Variant.DANGER, this::doFreeze)
        );
    }

    private boolean isFrozen() {
        AEGamePlayer gamePlayer = AEGamePlayer.fromUuid(player.getUuid());
        return gamePlayer != null && gamePlayer.isFrozen();
    }

    private void confirmBan() {
        showPopup(new AEModerationPopup(lang("ui.player.moderation.confirm_ban", args("player", AEUIPlayerDetailsView.getDisplayName(player))), this::applyBan));
    }

    private void confirmMute() {
        showPopup(new AEModerationPopup(lang("ui.player.moderation.confirm_mute", args("player", AEUIPlayerDetailsView.getDisplayName(player))), this::applyMute));
    }

    private void applyBan(String reason, Duration duration) {
        AEPlayersModerationModule moderation = getModerationModule();
        AEExecutionContext context = getActorContext();
        AEActionResult result = duration == null
                ? moderation.handleBan(context, player.getUuid(), reason)
                : moderation.handleTempBan(context, player.getUuid(), duration, reason);
        showNotification(result);
        refreshSelf();
    }

    private void applyMute(String reason, Duration duration) {
        AEPlayersModerationModule moderation = getModerationModule();
        AEExecutionContext context = getActorContext();
        AEActionResult result = duration == null
                ? moderation.handleMute(context, player.getUuid(), reason)
                : moderation.handleTempMute(context, player.getUuid(), duration, reason);
        showNotification(result);
        refreshSelf();
    }

    private void openEntryDetails(AEPlayerModerationHistoryEntry entry) {
        boolean isBan = "Ban".equals(entry.type());
        showPopup(new AEUIModerationEntryPopup(entry, AEUIPlayerDetailsView.getDisplayName(player),
                isBan ? this::applyBan : this::applyMute,
                isBan ? this::revokeBan : this::revokeMute));
    }

    private void revokeBan() {
        showNotification(getModerationModule().handleUnban(getActorContext(), player.getUuid()));
        refreshSelf();
    }

    private void revokeMute() {
        showNotification(getModerationModule().handleUnmute(getActorContext(), player.getUuid()));
        refreshSelf();
    }

    private void doKick() {
        showNotification(getModerationModule().handleKick(getActorContext(), player.getUuid(), lang("ui.player.moderation.kick_default_reason")));
    }

    private void doFreeze() {
        showNotification(getModerationModule().handleFreeze(getActorContext(), player.getUuid()));
        refreshSelf();
    }

    private void doUnfreeze() {
        showNotification(getModerationModule().handleUnfreeze(getActorContext(), player.getUuid()));
        refreshSelf();
    }

    private void refreshSelf() {
        refresh("Buttons", "Table");
    }

    private AEPlayersModerationModule getModerationModule() {
        return AEModulesManager.get().getModule(AEPlayersModule.class).getModerationModule();
    }
}
