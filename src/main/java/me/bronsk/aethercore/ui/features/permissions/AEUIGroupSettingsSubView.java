package me.bronsk.aethercore.ui.features.permissions;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.ui.components.AEInputField;
import me.bronsk.aethercore.ui.components.AEInputPreview;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.Map;

/**
 * Group Details' Settings sub-tab: the group's prefix, suffix and priority - a thin renderer over
 * {@link AEUIGroupDetailsView}'s draft state, same pattern as the Perms/Children/Players sub-tabs.
 * Prefix/Suffix use {@link AEInputPreview} (a live-updating colored preview of the raw value - the
 * same {@code &}-code resolution {@link AEMessageUtils#fromColoredString} uses for chat's own
 * settings preview), since both feed the {@code {prefix}}/{@code {suffix}} chat placeholders (see
 * {@code AEPermissionsModule}'s own registration of those two). Priority is a plain number, nothing
 * to preview, so it stays a bare {@link AEInputField}.
 */
final class AEUIGroupSettingsSubView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIGroupSettingsSubView.ui";

    private final AEUIGroupDetailsView parent;

    AEUIGroupSettingsSubView(AEUIGroupDetailsView parent) {
        this.parent = parent;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Prefix", new AEInputPreview(lang("ui.group.settings.prefix"),
                        new AETextInput(lang("ui.group.settings.prefix_placeholder"), parent.getDraftPrefix(), this::onPrefixChanged),
                        previewPrefix(parent.getDraftPrefix())),
                "Suffix", new AEInputPreview(lang("ui.group.settings.suffix"),
                        new AETextInput(lang("ui.group.settings.suffix_placeholder"), parent.getDraftSuffix(), this::onSuffixChanged),
                        previewSuffix(parent.getDraftSuffix())),
                "Priority", new AEInputField(lang("ui.group.settings.priority"), new AETextInput("0", String.valueOf(parent.getDraftPriority()), this::onPriorityChanged))
        );
    }

    private void onPrefixChanged(String value) {
        parent.setDraftPrefix(value);
        refresh("Prefix");
    }

    private void onSuffixChanged(String value) {
        parent.setDraftSuffix(value);
        refresh("Suffix");
    }

    private Message previewPrefix(String prefix) {
        return AEMessageUtils.fromColoredString(prefix == null ? "" : prefix);
    }

    private Message previewSuffix(String suffix) {
        return AEMessageUtils.fromColoredString(suffix == null ? "" : suffix);
    }

    private void onPriorityChanged(String value) {
        try {
            parent.setDraftPriority(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            // Leave the last valid priority in place until the admin types a real number.
        }
    }
}
