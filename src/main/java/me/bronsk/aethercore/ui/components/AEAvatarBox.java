package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/** Binds directly onto an already-declared avatar box element (e.g. {@link AEPlayerTag}'s #AvatarBox) - sets its own Background (when overridden) and the nested initial-letter label inside it. */
public final class AEAvatarBox extends AEUIComponent {

    private final String initial;
    private final String background;

    public AEAvatarBox(String initial, String background) {
        this.initial = initial;
        this.background = background;
    }

    @Override
    protected Map<String, Object> getContent() {
        Map<String, Object> content = new HashMap<>();
        content.put("Initial", new AELabel(initial));
        if (background != null) {
            content.put("Background", background);
        }
        return content;
    }
}
