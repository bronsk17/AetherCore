package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AETextInput;

import java.time.Duration;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A confirm/cancel popup for a moderation action that needs a reason and an optional duration
 * (e.g. Ban, Mute) - shown via {@link AEUIComponent#showPopup}. Cancel always just closes the
 * popup. Confirm parses the duration text (blank = permanent, invalid = ignored - the click simply
 * does nothing until the text is fixed) and, once valid, runs {@code onConfirm} and closes.
 */
public final class AEModerationPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEModerationPopup.ui";

    private final String title;
    private final BiConsumer<String, Duration> onConfirm;

    private String reason;
    private String durationText;

    public AEModerationPopup(String title, BiConsumer<String, Duration> onConfirm) {
        this(title, "", "", onConfirm);
    }

    /** Same dialog, pre-filled instead of blank - e.g. "Copy" re-issuing a past ban/mute's reason and duration. */
    public AEModerationPopup(String title, String initialReason, String initialDurationText, BiConsumer<String, Duration> onConfirm) {
        this.title = title;
        this.reason = initialReason;
        this.durationText = initialDurationText;
        this.onConfirm = onConfirm;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(title),
                "Content.ReasonInput", new AETextInput(lang("ui.moderation.reason_placeholder"), reason, this::onReasonChanged),
                "Content.DurationInput", new AETextInput(lang("ui.moderation.duration_placeholder"), durationText, this::onDurationChanged),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), true, this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
        );
    }

    private void onReasonChanged(String value) {
        reason = value;
    }

    private void onDurationChanged(String value) {
        durationText = value;
    }

    private void confirm() {
        Duration duration = null;
        if (!durationText.isBlank()) {
            duration = AEDurationArg.tryParse(durationText);
            if (duration == null) {
                return;
            }
        }
        onConfirm.accept(reason, duration);
        closePopup();
    }
}
