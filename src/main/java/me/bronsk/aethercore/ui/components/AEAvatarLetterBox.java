package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.Map;

/**
 * The fallback avatar shown while no downloaded image is available yet (or the download failed) -
 * a colored box with the player's initial letter. Unlike {@link AEAvatarBox} (which binds directly
 * onto an already-declared element, e.g. {@link AEPlayerTag}'s small tag), this owns its own
 * template so it can be appended/swapped freely into an empty container - see
 * {@code AEUIPlayerOverviewView}, which swaps this out for {@link AEAvatarImageBox} once a
 * hyvatar.io render finishes downloading.
 */
public final class AEAvatarLetterBox extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Player/AEAvatarLetterBox.ui";

    private final String initial;

    public AEAvatarLetterBox(String initial) {
        this.initial = initial;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Initial", new AELabel(initial));
    }
}
