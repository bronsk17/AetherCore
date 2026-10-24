package me.bronsk.aethercore.ui.features.admintools;

import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Adventure/Creative dropdown - the only two gamemodes the native SDK's simple {@code GameMode} enum supports (Spectator is a separate overlay toggle, not a gamemode value here). */
final class AEGamemodeSelector extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/AdminTools/AEGamemodeSelector.ui";

    private final String value;
    private final Consumer<String> onChange;

    AEGamemodeSelector(String value, Consumer<String> onChange) {
        this.value = value;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<DropdownEntryInfo> entries = List.of(
                new DropdownEntryInfo(LocalizableString.fromString(lang("ui.admintools.gamemode.adventure")), "Adventure"),
                new DropdownEntryInfo(LocalizableString.fromString(lang("ui.admintools.gamemode.creative")), "Creative")
        );
        return Map.of("Entries", entries, "Value", value, "OnValueChanged", onChange);
    }
}
