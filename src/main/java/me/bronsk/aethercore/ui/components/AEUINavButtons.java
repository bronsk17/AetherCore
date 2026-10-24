package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;

/**
 * Back/forward history buttons - self-contained, no wiring needed from the caller. Binds directly
 * onto whatever scope it's given (no own template) - it shares the sidebar's single card background
 * with the tab list below it (see {@code Main.ui}'s {@code #Panel}).
 */
public final class AEUINavButtons extends AEUIComponent {

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("", List.of(
                new AESmallButton("<", this::goBack),
                new AESmallButton(">", this::goForward),
                new AEIconButton(AEIconButton.Icon.REFRESH, this::refreshCurrentView)
        ));
    }
}
