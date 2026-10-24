package me.bronsk.aethercore.ui.features.players;

import com.hypixel.hytale.server.core.universe.Universe;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEMutedLabel;
import me.bronsk.aethercore.ui.components.AEPlainGroup;
import me.bronsk.aethercore.ui.components.AEStatusDot;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * The player-details view: just the shell + sub-tab switch. Overview ({@link
 * AEUIPlayerOverviewView}) and Moderation ({@link AEUIPlayerModerationView}) are separate
 * components, each with its own template - this class owns none of their content. {@code subTabs}
 * is kept as a field (not rebuilt per {@link #getContent()}) so its own selection state survives
 * across refreshes - see {@link AEUITabs}.
 */
public final class AEUIPlayerDetailsView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Components/Shells/SubTabsWithTitleShell.ui";

    private enum SubTab { OVERVIEW, MODERATION }

    private final AEPlayer player;
    private final AEUITabs subTabs;

    public AEUIPlayerDetailsView(AEPlayer player) {
        this.player = player;
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.player.overview", SubTab.OVERVIEW, () -> new AEUIPlayerOverviewView(player)),
                new AEUITabDef("ui.tab.player.moderation", SubTab.MODERATION, () -> new AEUIPlayerModerationView(player))
        ), SubTab.OVERVIEW, false, this::onSubTabChanged);
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        boolean online = isOnline(player);
        List<AEUIComponent> idParts = online
                ? List.of(new AEMutedLabel("#" + player.getId()), new AEStatusDot(true))
                : List.of(new AEMutedLabel("#" + player.getId()));
        return Map.of(
                "Title.Main", new AELabel(lang("ui.player.title", args("player", getDisplayName(player)))),
                "Title.Id", List.of(new AEPlainGroup("CenterMiddle", idParts)),
                "SubTabs", subTabs,
                "Details", subTabs.current()
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }

    static boolean isOnline(AEPlayer player) {
        try {
            return Universe.get().getPlayer(UUID.fromString(player.getUuid())) != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }
}
