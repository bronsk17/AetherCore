package me.bronsk.aethercore.ui.features.permissions;

import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import me.bronsk.aethercore.commands.arguments.AEDurationArg;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AETextInput;
import me.bronsk.aethercore.utils.AEDateUtils;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Shown when a row in a staged/persisted table is clicked. {@code onRemove}, when supplied, stages
 * the row's removal (or discards it outright if the caller treats it as a not-yet-saved addition);
 * a null {@code onRemove} hides the Remove button entirely - used by tables where deletion happens
 * only via their own checkbox-selection + batch-delete button, not per-row from this popup. "Cancel"
 * always closes without any change. Every button row is ordered the same way across every kind: the
 * red action (Remove, when present) first, then Save when there's something to save, then Cancel
 * last. Rows backed by a per-relation metadata table (a player's group membership or direct
 * permission grant) show World/Expires fields two ways depending on {@link Kind}: {@link
 * Kind#EDITABLE} for a still-staged (not-yet-saved) row, where World (a {@link AEWorldSelector} over
 * every registered world) and "expires in" (e.g. "7d", like a ban's duration - shown verbatim as
 * whatever text was last typed, never reformatted from a recomputed remaining time, which would
 * drift with the clock) are editable and have their own Save that stages the metadata alongside the
 * row; {@link Kind#READONLY} for an already-persisted row with metadata set, where the fields are
 * shown as plain text and can no longer be changed (changing metadata means removing and re-adding
 * the row) - here "expires at" is shown as the actual absolute date/time it was resolved to on save,
 * the same "set as duration, store and show as a date" convention already used for ban/mute expiry.
 */
final class AEUIRowDetailsPopup extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Permissions/AEUIRowDetailsPopup.ui";
    private static final String TEMPLATE_METADATA = "Pages/AetherCore/Features/Permissions/AEUIRowDetailsPopupMetadata.ui";
    private static final String TEMPLATE_METADATA_READONLY = "Pages/AetherCore/Features/Permissions/AEUIRowDetailsPopupMetadataReadonly.ui";

    /** Carries the raw expiry text alongside the resolved Timestamp so the caller can remember exactly what was typed - see the class doc. */
    @FunctionalInterface
    interface MetadataSaveHandler {
        void accept(String worldName, Timestamp expiresAt, String expiresInText);
    }

    private enum Kind { SIMPLE, EDITABLE, READONLY }

    private final String title;
    private final Kind kind;
    private String worldName;
    private String expiresInText;
    private final Timestamp readonlyExpiresAt;
    private final MetadataSaveHandler onSaveMetadata;
    private final Runnable onRemove;

    /** No metadata to show - just Remove/Cancel. */
    AEUIRowDetailsPopup(String title, Runnable onRemove) {
        this(title, Kind.SIMPLE, null, null, null, null, onRemove);
    }

    /** Metadata can still be set (the row is a staged, not-yet-saved addition) - editable World/expires-in fields plus their own Save, alongside Remove/Cancel. {@code initialExpiresInText} is shown verbatim - see the class doc. */
    AEUIRowDetailsPopup(String title, String initialWorldName, String initialExpiresInText,
                         MetadataSaveHandler onSaveMetadata, Runnable onRemove) {
        this(title, Kind.EDITABLE, initialWorldName, initialExpiresInText, null, onSaveMetadata, onRemove);
    }

    /** Same as above, but with no Remove button - deletion happens only via the table's own checkbox-selection + batch-delete button. */
    AEUIRowDetailsPopup(String title, String initialWorldName, String initialExpiresInText, MetadataSaveHandler onSaveMetadata) {
        this(title, Kind.EDITABLE, initialWorldName, initialExpiresInText, null, onSaveMetadata, null);
    }

    /** Metadata is already set and immutable (the row is persisted) - shown as read-only text (World, absolute Expires At) alongside Remove/Cancel. */
    AEUIRowDetailsPopup(String title, String worldName, Timestamp expiresAt, Runnable onRemove) {
        this(title, Kind.READONLY, worldName, null, expiresAt, null, onRemove);
    }

    /** Same as above, but with no Remove button - deletion happens only via the table's own checkbox-selection + batch-delete button. */
    AEUIRowDetailsPopup(String title, String worldName, Timestamp expiresAt) {
        this(title, Kind.READONLY, worldName, null, expiresAt, null, null);
    }

    private AEUIRowDetailsPopup(String title, Kind kind, String worldName, String expiresInText, Timestamp readonlyExpiresAt,
                                 MetadataSaveHandler onSaveMetadata, Runnable onRemove) {
        this.title = title;
        this.kind = kind;
        this.worldName = worldName == null ? "" : worldName;
        this.expiresInText = expiresInText == null ? "" : expiresInText;
        this.readonlyExpiresAt = readonlyExpiresAt;
        this.onSaveMetadata = onSaveMetadata;
        this.onRemove = onRemove;
    }

    @Override
    protected String getTemplate() {
        return switch (kind) {
            case SIMPLE -> TEMPLATE;
            case EDITABLE -> TEMPLATE_METADATA;
            case READONLY -> TEMPLATE_METADATA_READONLY;
        };
    }

    @Override
    protected Map<String, Object> getContent() {
        if (kind == Kind.SIMPLE) {
            return Map.of(
                    "Title.TitleLabel", new AELabel(title),
                    "Content.RemoveButton", getRemoveButton(),
                    "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
            );
        }
        if (kind == Kind.READONLY) {
            return Map.of(
                    "Title.TitleLabel", new AELabel(title),
                    "Content.WorldInput", new AELabel(worldName.isBlank() ? lang("ui.common.any") : worldName),
                    "Content.ExpiresInput", new AELabel(readonlyExpiresAt == null ? lang("ui.common.never") : AEDateUtils.formatByConfig(readonlyExpiresAt)),
                    "Content.RemoveButton", getRemoveButton(),
                    "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
            );
        }
        return Map.of(
                "Title.TitleLabel", new AELabel(title),
                "Content.WorldInput", new AEWorldSelector(getWorldNames(), worldName, this::onWorldChanged),
                "Content.ExpiresInput", new AETextInput(lang("ui.permissions.expires_in_placeholder"), expiresInText, this::onExpiresChanged),
                "Content.RemoveButton", getRemoveButton(),
                "Content.SaveButton", new AEActionButton(lang("ui.common.save"), this::save),
                "Content.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::closePopup)
        );
    }

    private List<String> getWorldNames() {
        return Universe.get().getWorlds().values().stream()
                .filter(world -> world != null && world.getName() != null && !world.getName().isBlank())
                .map(World::getName)
                .sorted()
                .toList();
    }

    private List<AEUIComponent> getRemoveButton() {
        return onRemove == null ? List.of() : List.of(new AEActionButton(lang("ui.common.remove"), true, this::remove));
    }

    private void onWorldChanged(String value) {
        worldName = value;
    }

    private void onExpiresChanged(String value) {
        expiresInText = value;
    }

    private void save() {
        Timestamp expiresAt = null;
        String trimmedExpires = expiresInText.trim();
        if (!trimmedExpires.isEmpty()) {
            Duration duration = AEDurationArg.tryParse(trimmedExpires);
            if (duration == null) {
                showNotification(AEActionResult.error(AEMessageUtils.fromColoredString(lang("ui.permissions.invalid_duration"))));
                return;
            }
            expiresAt = new Timestamp(System.currentTimeMillis() + duration.toMillis());
        }
        String trimmedWorld = worldName.trim();
        onSaveMetadata.accept(trimmedWorld.isEmpty() ? null : trimmedWorld, expiresAt, trimmedExpires.isEmpty() ? null : trimmedExpires);
        closePopup();
    }

    private void remove() {
        onRemove.run();
        closePopup();
    }
}
