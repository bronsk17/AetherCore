package me.bronsk.aethercore.ui.components;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a {@link Message} as colored text, line by line, so a resolved chat/lang message preview
 * shows its real final colors instead of raw {@code &}-codes - see {@link
 * me.bronsk.aethercore.utils.AEMessageUtils} for how a colored string becomes this kind of {@link
 * Message} tree, and {@link AEColorRun} for why each run is its own small structurally-colored Label
 * rather than one dynamically-colored Label. A message's text can contain literal newlines (lang
 * translations are edited via a multiline input), so each line is its own {@link AEColoredTextLine}
 * row, stacked top to bottom - a single flat row of runs can't represent a line break.
 */
public final class AEColoredText extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Labels/AEColoredText.ui";

    private final Message message;

    public AEColoredText(Message message) {
        this.message = message;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<List<AEUIComponent>> lines = new ArrayList<>();
        lines.add(new ArrayList<>());
        if (message != null) {
            collectRuns(message, null, lines);
        }
        if (lines.size() == 1 && lines.get(0).isEmpty()) {
            lines.get(0).add(new AEColorRun("-", null));
        }
        List<AEUIComponent> rows = lines.stream().map(line -> (AEUIComponent) new AEColoredTextLine(line)).toList();
        return Map.of("", rows);
    }

    private void collectRuns(Message node, String inheritedColor, List<List<AEUIComponent>> lines) {
        String color = node.getColor() != null ? node.getColor() : inheritedColor;
        String rawText = node.getRawText();
        if (rawText != null && !rawText.isEmpty()) {
            appendText(rawText, color, lines);
        }
        for (Message child : node.getChildren()) {
            collectRuns(child, color, lines);
        }
    }

    private void appendText(String text, String color, List<List<AEUIComponent>> lines) {
        String[] segments = text.split("\n", -1);
        for (int index = 0; index < segments.length; index++) {
            if (!segments[index].isEmpty()) {
                lines.get(lines.size() - 1).add(new AEColorRun(segments[index], color));
            }
            if (index < segments.length - 1) {
                lines.add(new ArrayList<>());
            }
        }
    }
}
