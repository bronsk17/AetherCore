package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.modules.player.models.AEPlayerModerationHistoryEntry;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEDetailRow;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.utils.AEDateUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * Read-only view of one {@link AEPlayerModerationHistoryEntry} (a past ban or mute), opened by
 * clicking a Moderation History row. Copy opens the same dialog a fresh Ban/Mute would, pre-filled
 * with this entry's reason and duration *length* (not remaining time). Revoke is only offered while
 * this entry is still the active one.
 */
public final class AEUIModerationEntryPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEUIModerationEntryPopup.ui";

    private final AEPlayerModerationHistoryEntry entry;
    private final String targetName;
    private final BiConsumer<String, Duration> reissue;
    private final Runnable revoke;

    public AEUIModerationEntryPopup(AEPlayerModerationHistoryEntry entry, String targetName,
                                       BiConsumer<String, Duration> reissue, Runnable revoke) {
        this.entry = entry;
        this.targetName = targetName;
        this.reissue = reissue;
        this.revoke = revoke;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(lang("ui.moderation.entry.title", args("type", entry.type()))),
                "Content.Rows", getRows(),
                "Content.CopyButton", new AEActionButton(lang("ui.common.copy"), this::copy),
                "Content.RevokeButton", isActive()
                        ? List.of(new AEActionButton(lang("ui.moderation.revoke"), true, this::doRevoke))
                        : List.of(),
                "Content.ExitButton", new AEActionButton(lang("ui.common.exit"), this::closePopup)
        );
    }

    private List<AEUIComponent> getRows() {
        return List.of(
                new AEDetailRow(lang("ui.moderation.entry.status"), entry.status()),
                new AEDetailRow(lang("ui.moderation.entry.reason"), entry.reason()),
                new AEDetailRow(lang("ui.moderation.entry.by"), entry.by()),
                new AEDetailRow(lang("ui.moderation.entry.revoked_by"), entry.revokedBy()),
                new AEDetailRow(lang("ui.moderation.entry.created_at"), AEDateUtils.formatByConfig(entry.createdAt())),
                new AEDetailRow(lang("ui.moderation.entry.expires_at"), entry.expiresAt() == null ? lang("ui.common.never") : AEDateUtils.formatByConfig(entry.expiresAt()))
        );
    }

    private boolean isActive() {
        return "Active".equals(entry.status());
    }

    private void copy() {
        String durationText = entry.expiresAt() == null
                ? ""
                : AEDurationArg.format(Duration.between(entry.createdAt().toInstant(), entry.expiresAt().toInstant()));
        showPopup(new AEModerationPopup(lang("ui.moderation.entry.confirm_reissue", args("type", entry.type(), "player", targetName)), entry.reason(), durationText, reissue));
    }

    private void doRevoke() {
        revoke.run();
        closePopup();
    }
}
