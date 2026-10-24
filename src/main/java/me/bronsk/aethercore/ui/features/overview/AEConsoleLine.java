package me.bronsk.aethercore.ui.features.overview;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/** One line of the Overview console panel - its own dynamically-colored Label (plain hex only - see the project's own hard-learned rule that alpha-paren colors break when set dynamically) so each line can carry the real console's own per-level color. */
final class AEConsoleLine extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Overview/AEConsoleLine.ui";

    private final String text;
    private final String color;

    AEConsoleLine(String text, String color) {
        this.text = text;
        this.color = color;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Text", text);
        if (color != null) {
            content.put("Style.TextColor", color);
        }
        return content;
    }
}
