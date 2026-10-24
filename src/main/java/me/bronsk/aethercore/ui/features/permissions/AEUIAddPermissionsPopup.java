package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEEmptyMessage;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AETextInput;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * "Add Permissions" popup: a text box suggests permission nodes already granted to some other
 * group or player, but typing something new is never blocked - its own Add button (or Enter)
 * stages whatever is currently typed regardless of whether it matched a suggestion. Clicking a
 * suggestion chip stages it the same way. Confirm hands every staged node back to the caller -
 * which decides what that means (persist immediately, or accumulate into a draft applied only
 * when the page's own Save is pressed).
 */
public final class AEUIAddPermissionsPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIAddPermissionsPopup.ui";

    private final String title;
    private final Set<String> excluded;
    private final Consumer<Collection<String>> onConfirm;
    private final LinkedHashSet<String> staged = new LinkedHashSet<>();
    private String search = "";

    public AEUIAddPermissionsPopup(String title, Set<String> excluded, Consumer<Collection<String>> onConfirm) {
        this.title = title;
        this.excluded = excluded;
        this.onConfirm = onConfirm;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(title),
                "Content.Search", new AETextInput(lang("ui.permnode.search_placeholder"), search, this::onSearchChanged),
                "Content.AddButton", new AEActionButton(lang("ui.common.add"), this::addTyped),
                "Content.Results", getResultTags(),
                "Content.Staged", getStagedTags(),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
        );
    }

    private List<AEUIComponent> getResultTags() {
        String needle = search.toLowerCase(Locale.ROOT);
        List<String> matches = getPermissionsModule().getAllKnownPermissions().stream()
                .filter(node -> !excluded.contains(node) && !staged.contains(node))
                .filter(node -> needle.isBlank() || node.toLowerCase(Locale.ROOT).contains(needle))
                .sorted()
                .toList();
        if (matches.isEmpty()) {
            return List.of(new AEEmptyMessage(search.isBlank() ? lang("ui.permnode.type_to_search") : lang("ui.permnode.no_matches")));
        }
        return matches.stream().map(node -> (AEUIComponent) new AEGroupTag(node, false, () -> stage(node))).toList();
    }

    private List<AEUIComponent> getStagedTags() {
        if (staged.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.permnode.none_staged")));
        }
        return staged.stream().map(node -> (AEUIComponent) new AEGroupTag(node, true, () -> unstage(node))).toList();
    }

    private void onSearchChanged(String value) {
        search = value;
        refresh("Content.Results");
    }

    private void addTyped() {
        String node = search.trim();
        if (node.isEmpty() || excluded.contains(node) || staged.contains(node)) {
            return;
        }
        staged.add(node);
        search = "";
        refresh("Content.Search", "Content.Results", "Content.Staged");
    }

    private void stage(String node) {
        staged.add(node);
        refresh("Content.Results", "Content.Staged");
    }

    private void unstage(String node) {
        staged.remove(node);
        refresh("Content.Results", "Content.Staged");
    }

    private void confirm() {
        onConfirm.accept(staged);
        closePopup();
    }

    private AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }
}
