package me.bronsk.aethercore.ui.components;

import me.bronsk.aethercore.ui.framework.AEUIComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * A small "[avatar-initial][name]" identity pill - reusable anywhere a player needs to show up as a
 * clickable/colorable chip (e.g. the Dupe IP lookup, one row per matching player). {@code onClick}
 * is nullable: null picks a plain non-interactive template, non-null a clickable one with native
 * hover - same "two templates, clickable vs not" split already used by {@link AETableRow}, rather
 * than guessing at click support on a bare {@code Group}. The clickable templates keep their own
 * per-variant colors baked in (a hover-state {@code Style} needs a template, confirmed unsafe to set
 * dynamically); the non-interactive ones share one template with {@code variant}'s colors set
 * dynamically instead, since a plain {@code Group}'s flat {@code Background} - unlike a clickable
 * {@code Button}'s multi-state one - is just a settable leaf.
 */
public final class AEPlayerTag extends AEUIComponent {

    /** Fixed at construction - a real color/click change needs a new instance. */
    public enum Variant { NEUTRAL, DANGER, SUCCESS, MUTED }

    private static final String TEMPLATE = "Pages/AetherCore/Components/Player/AEPlayerTag.ui";
    private static final String TEMPLATE_NEUTRAL_CLICKABLE = "Pages/AetherCore/Components/Player/AEPlayerTagNeutralClickable.ui";
    private static final String TEMPLATE_DANGER_CLICKABLE = "Pages/AetherCore/Components/Player/AEPlayerTagDangerClickable.ui";
    private static final String TEMPLATE_SUCCESS_CLICKABLE = "Pages/AetherCore/Components/Player/AEPlayerTagSuccessClickable.ui";
    private static final String TEMPLATE_MUTED_CLICKABLE = "Pages/AetherCore/Components/Player/AEPlayerTagMutedClickable.ui";

    private final String initial;
    private final String name;
    private final Variant variant;
    private final Runnable onClick;

    public AEPlayerTag(String initial, String name, Variant variant) {
        this(initial, name, variant, null);
    }

    public AEPlayerTag(String initial, String name, Variant variant, Runnable onClick) {
        this.initial = initial;
        this.name = name;
        this.variant = variant;
        this.onClick = onClick;
    }

    @Override
    protected String getTemplate() {
        if (onClick == null) {
            return TEMPLATE;
        }
        return switch (variant) {
            case DANGER -> TEMPLATE_DANGER_CLICKABLE;
            case SUCCESS -> TEMPLATE_SUCCESS_CLICKABLE;
            case MUTED -> TEMPLATE_MUTED_CLICKABLE;
            case NEUTRAL -> TEMPLATE_NEUTRAL_CLICKABLE;
        };
    }

    @Override
    protected Map<String, Object> getContent() {
        boolean clickable = onClick != null;
        Map<String, Object> content = new HashMap<>();
        content.put("AvatarBox", new AEAvatarBox(initial, clickable ? null : avatarBackground(variant)));
        content.put("Name", new AELabel(name, clickable ? null : textColor(variant)));
        if (clickable) {
            content.put("OnActivate", onClick);
        } else {
            content.put("Background", tagBackground(variant));
        }
        return content;
    }

    private static String tagBackground(Variant variant) {
        return switch (variant) {
            case NEUTRAL -> "#2a3a52(0.6)";
            case DANGER -> "#7a1f1f(0.55)";
            case SUCCESS -> "#1f6b3a(0.55)";
            case MUTED -> "#3a3f4a(0.5)";
        };
    }

    private static String avatarBackground(Variant variant) {
        return switch (variant) {
            case NEUTRAL -> "#22344a";
            case DANGER -> "#5c1717";
            case SUCCESS -> "#174f2c";
            case MUTED -> "#2c303a";
        };
    }

    private static String textColor(Variant variant) {
        return variant == Variant.MUTED ? "#ffffff(0.7)" : "#ffffff(0.9)";
    }
}
