package me.bronsk.aethercore.ui.components;

import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The page-size dropdown used by {@link AEPagination} - not reusable outside it, hence package-private. */
final class AEPageSizeDropdown extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Components/Pagination/AEPageSizeDropdown.ui";

    private final List<DropdownEntryInfo> entries;
    private final String value;
    private final Consumer<String> onChange;

    AEPageSizeDropdown(List<DropdownEntryInfo> entries, String value, Consumer<String> onChange) {
        this.entries = entries;
        this.value = value;
        this.onChange = onChange;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of("Entries", entries, "Value", value, "OnValueChanged", onChange);
    }
}
