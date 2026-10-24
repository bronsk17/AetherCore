package me.bronsk.aethercore.ui.features.permissions;

import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** World-picker dropdown for permission/group metadata's World field - a leading "Any" entry (empty value) means no world restriction, same as the free-text field it replaces used to mean by being left empty. */
final class AEWorldSelector extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEWorldSelector.ui";
    static final String ANY_VALUE = "";

    private final List<String> worldNames;
    private final String value;
    private final Consumer<String> onChange;

    AEWorldSelector(List<String> worldNames, String value, Consumer<String> onChange) {
        this.worldNames = worldNames;
        this.value = value;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<DropdownEntryInfo> entries = new ArrayList<>();
        entries.add(new DropdownEntryInfo(LocalizableString.fromString(lang("ui.common.any")), ANY_VALUE));
        for (String worldName : worldNames) {
            entries.add(new DropdownEntryInfo(LocalizableString.fromString(worldName), worldName));
        }
        return Map.of("Entries", entries, "Value", value == null ? ANY_VALUE : value, "OnValueChanged", onChange);
    }
}
