package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.HashMap;
import java.util.Map;

/** The colored strip shown at the top of a page after a UI action - green on success, red on error. */
public final class AENotificationBanner extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Notifications/AENotificationBanner.ui";
    private static final String COLOR_SUCCESS = "#1f8a4c";
    private static final String COLOR_ERROR = "#b3261e";

    private final AEActionResult result;

    public AENotificationBanner(AEActionResult result) {
        this.result = result;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Background", result.success() ? COLOR_SUCCESS : COLOR_ERROR);
        content.put("Text", new AELabel(AEMessageUtils.toPlainText(result.message())));
        return content;
    }
}
