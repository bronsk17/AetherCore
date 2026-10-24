package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEEmptyMessage;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEPlayerTag;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/** Read-only popup: every other player who last connected from the same IP, tinted by live status, click to jump to that player. */
public final class AEUIDupeIpPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEUIDupeIpPopup.ui";

    private final String ip;
    private final List<AEPlayer> players;
    private final Function<AEPlayer, AEPlayerTag.Variant> statusResolver;
    private final Consumer<AEPlayer> onSelect;

    public AEUIDupeIpPopup(String ip, List<AEPlayer> players, Function<AEPlayer, AEPlayerTag.Variant> statusResolver, Consumer<AEPlayer> onSelect) {
        this.ip = ip;
        this.players = players;
        this.statusResolver = statusResolver;
        this.onSelect = onSelect;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.TitleLabel", new AELabel(lang("ui.player.dupe_ip.title", args("ip", ip))),
                "Content.Rows", getRows(),
                "Content.ExitButton", new AEActionButton(lang("ui.common.exit"), this::closePopup)
        );
    }

    private List<AEUIComponent> getRows() {
        if (players.isEmpty()) {
            return List.of(new AEEmptyMessage(lang("ui.player.dupe_ip.empty")));
        }
        return players.stream()
                .map(player -> (AEUIComponent) new AEPlayerTag(getInitial(player), getDisplayName(player),
                        statusResolver.apply(player), () -> onSelect.accept(player)))
                .toList();
    }

    private static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }

    private static String getInitial(AEPlayer player) {
        String name = getDisplayName(player);
        return name.substring(0, 1).toUpperCase(Locale.ROOT);
    }
}
