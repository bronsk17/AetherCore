package me.bronsk.aethercore.ui.features.players;

import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEAvatarImageBox;
import me.bronsk.aethercore.ui.components.AEAvatarLetterBox;
import me.bronsk.aethercore.ui.components.AEDetailRow;

import java.util.List;
import java.util.Map;

/** A player details panel: an avatar on the left, and to its right a borderless label/value table (uuid, created at, ...) with the utility action buttons below it. */
public final class AEPlayerDetails extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Players/AEPlayerDetails.ui";

    private final String avatarInitial;
    private final String avatarAssetPath;
    private final List<Row> rows;
    private final List<AEUIComponent> actions;
    private final List<AEUIComponent> cards;

    /** {@code avatarAssetPath} is null until a hyvatar.io render has actually finished downloading and been registered - see {@code AEUIPlayerOverviewView}. {@code cards} sit below the actions, to the right of the avatar - not spanning the avatar's own column. */
    public AEPlayerDetails(String avatarInitial, String avatarAssetPath, List<Row> rows, List<AEUIComponent> actions, List<AEUIComponent> cards) {
        this.avatarInitial = avatarInitial;
        this.avatarAssetPath = avatarAssetPath;
        this.rows = rows;
        this.actions = actions;
        this.cards = cards;
    }

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Avatar", avatarAssetPath == null ? new AEAvatarLetterBox(avatarInitial) : new AEAvatarImageBox(avatarAssetPath),
                "Content.Rows", getRows(),
                "Content.Actions", actions,
                "Content.Cards", cards
        );
    }

    private List<AEUIComponent> getRows() {
        return rows.stream()
                .map(row -> (AEUIComponent) new AEDetailRow(row.label(), row.value()))
                .toList();
    }

    /** One label/value row in the details table (e.g. "UUID" / the player's uuid). */
    public record Row(String label, String value) {
    }
}
