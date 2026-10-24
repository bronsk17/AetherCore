package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * Shows a live-downloaded avatar image via the native {@code AssetImage} element, once one has
 * actually finished downloading and been registered as a common asset - see {@code
 * AEDynamicImageAsset#register}. Until then, {@code AEUIPlayerOverviewView} shows {@link
 * AEAvatarLetterBox} instead.
 */
public final class AEAvatarImageBox extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Player/AEAvatarImageBox.ui";

    private final String assetPath;

    public AEAvatarImageBox(String assetPath) {
        this.assetPath = assetPath;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("AssetPath", assetPath);
    }
}
