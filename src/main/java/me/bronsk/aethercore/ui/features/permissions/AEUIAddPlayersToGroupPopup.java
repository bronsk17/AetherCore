package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEEmptyMessage;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEPlayerTag;
import me.bronsk.aethercore.ui.components.AESearch;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * "Add Players" popup: a search box drives a live-filtered list of players, clicking one stages
 * it as a chip below, Confirm hands every staged player back to the caller - which decides what
 * that means (the caller always stages into a draft, applied only on the page's own Save).
 */
public final class AEUIAddPlayersToGroupPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIAddPlayersToGroupPopup.ui";
    private static final int SEARCH_PAGE_SIZE = 8;

    private final String title;
    private final Set<String> excludedUuids;
    private final Consumer<Collection<AEPlayer>> onConfirm;
    private final Map<String, AEPlayer> staged = new LinkedHashMap<>();
    private String search = "";

    public AEUIAddPlayersToGroupPopup(String title, Set<String> excludedUuids, Consumer<Collection<AEPlayer>> onConfirm) {
        this.title = title;
        this.excludedUuids = excludedUuids;
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
                "Content.Search", new AESearch(lang("ui.players.search_placeholder"), search, this::onSearchChanged),
                "Content.Results", getResultTags(),
                "Content.Staged", getStagedTags(),
                "Content.ConfirmButton", new AEActionButton(lang("ui.common.confirm"), this::confirm),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
        );
    }

    private List<AEUIComponent> getResultTags() {
        if (search.isBlank()) {
            return List.of(new AEEmptyMessage(lang("ui.players.type_to_search")));
        }
        AEPageResult<AEPlayer> result = AEModulesManager.get().getModule(AEPlayersModule.class)
                .getPlayerQueryResult(new AEPageQuery(0, SEARCH_PAGE_SIZE, search));
        List<AEPlayer> matches = result.items().stream()
                .filter(player -> !staged.containsKey(player.getUuid()) && !excludedUuids.contains(player.getUuid()))
                .toList();
        if (matches.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.players.no_matches")));
        }
        return matches.stream()
                .map(player -> (AEUIComponent) new AEPlayerTag(getInitial(player), getDisplayName(player), AEPlayerTag.Variant.NEUTRAL, () -> stage(player)))
                .toList();
    }

    private List<AEUIComponent> getStagedTags() {
        if (staged.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.players.none_staged")));
        }
        return staged.values().stream()
                .map(player -> (AEUIComponent) new AEPlayerTag(getInitial(player), getDisplayName(player), AEPlayerTag.Variant.SUCCESS, () -> unstage(player)))
                .toList();
    }

    private void onSearchChanged(String value) {
        search = value;
        refresh("Content.Results");
    }

    private void stage(AEPlayer player) {
        staged.put(player.getUuid(), player);
        refresh("Content.Results", "Content.Staged");
    }

    private void unstage(AEPlayer player) {
        staged.remove(player.getUuid());
        refresh("Content.Results", "Content.Staged");
    }

    private void confirm() {
        onConfirm.accept(staged.values());
        closePopup();
    }

    private static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }

    private static String getInitial(AEPlayer player) {
        return getDisplayName(player).substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
