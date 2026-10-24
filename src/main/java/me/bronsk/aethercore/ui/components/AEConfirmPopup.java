package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * A confirm/cancel popup, shown via {@link AEUIComponent#showPopup}. Confirming runs the action
 * and closes the popup. Cancel just closes the popup by default (there's usually nothing else to
 * do); pass an explicit {@code onCancel} for the rarer case where cancelling needs to go somewhere
 * specific (e.g. reopening the popup that opened this confirm) instead of just closing.
 */
public final class AEConfirmPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Popups/AEConfirmPopup.ui";

    private final String title;
    private final String message;
    private final Runnable onConfirm;
    private final Runnable onCancel;

    public AEConfirmPopup(String title, String message, Runnable onConfirm) {
        this(title, message, onConfirm, null);
    }

    public AEConfirmPopup(String title, String message, Runnable onConfirm, Runnable onCancel) {
        this.title = title;
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(title),
                "Content.MessageLabel", new AELabel(message),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel)
        );
    }

    private void confirm() {
        onConfirm.run();
        closePopup();
    }

    private void cancel() {
        if (onCancel != null) {
            onCancel.run();
        } else {
            closePopup();
        }
    }
}
