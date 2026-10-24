package me.bronsk.aethercore.ui.features.lang;

import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The language dropdown used by {@link AEUILangMessagePopup} - not reusable outside it, hence package-private. */
final class AELangSelector extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Lang/AELangSelector.ui";

    private final List<String> langCodes;
    private final String value;
    private final Consumer<String> onChange;

    AELangSelector(List<String> langCodes, String value, Consumer<String> onChange) {
        this.langCodes = langCodes;
        this.value = value;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        List<DropdownEntryInfo> entries = langCodes.stream()
                .map(langCode -> new DropdownEntryInfo(LocalizableString.fromString(langCode), langCode))
                .toList();
        return Map.of("Entries", entries, "Value", value, "OnValueChanged", onChange);
    }
}
