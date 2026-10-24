package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEEmptyMessage;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AESearch;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * "Add Group(s)" popup: a search box drives a live-filtered list of registered groups, clicking
 * one stages it as a chip below, Confirm hands every staged group back to the caller - which
 * decides what that means (persist immediately, or accumulate into a draft applied only when the
 * page's own Save is pressed). Reused wherever an admin needs to pick from existing groups only -
 * a player's group membership, or a group's own inheritance children.
 */
public final class AEUIAddGroupsPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIAddGroupsPopup.ui";

    private final String title;
    private final Set<String> excluded;
    private final Consumer<Collection<String>> onConfirm;
    private final Set<String> staged = new LinkedHashSet<>();
    private String search = "";

    public AEUIAddGroupsPopup(String title, Set<String> excluded, Consumer<Collection<String>> onConfirm) {
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
                "Content.Search", new AESearch(lang("ui.groups.search_placeholder"), search, this::onSearchChanged),
                "Content.Results", getResultTags(),
                "Content.Staged", getStagedTags(),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
        );
    }

    private List<AEUIComponent> getResultTags() {
        String needle = search.toLowerCase(Locale.ROOT);
        List<String> matches = getPermissionsModule().getGroupsModule().getAllRegisteredGroups().stream()
                .filter(name -> !excluded.contains(name) && !staged.contains(name))
                .filter(name -> needle.isBlank() || name.toLowerCase(Locale.ROOT).contains(needle))
                .sorted()
                .toList();
        if (matches.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.groups.no_matches")));
        }
        return matches.stream().map(name -> (AEUIComponent) new AEGroupTag(name, false, () -> stage(name))).toList();
    }

    private List<AEUIComponent> getStagedTags() {
        if (staged.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.groups.none_staged")));
        }
        return staged.stream().map(name -> (AEUIComponent) new AEGroupTag(name, true, () -> unstage(name))).toList();
    }

    private void onSearchChanged(String value) {
        search = value;
        refresh("Content.Results");
    }

    private void stage(String name) {
        staged.add(name);
        refresh("Content.Results", "Content.Staged");
    }

    private void unstage(String name) {
        staged.remove(name);
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
