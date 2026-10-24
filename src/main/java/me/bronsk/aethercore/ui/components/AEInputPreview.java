package me.bronsk.aethercore.ui.components;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * One settings-form row: a label, an arbitrary input control, and a live preview below it. The
 * preview is a real {@link Message} so a colored template (e.g. a resolved chat message) renders
 * with its actual final colors, not raw {@code &}-codes - see {@link AELabel#AELabel(Message)}.
 */
public final class AEInputPreview extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Inputs/AEInputPreview.ui";

    private final String label;
    private final AEUIComponent input;
    private final Message preview;

    public AEInputPreview(String label, AEUIComponent input, Message preview) {
        this.label = label;
        this.input = input;
        this.preview = preview;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Label", new AELabel(label),
                "Input", input,
                "Preview", new AEColoredText(preview)
        );
    }
}
