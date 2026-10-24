package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerPermission;
import me.bronsk.aethercore.modules.permissions.models.AEUserPermissionMetadata;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEUITabs;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One player's permissions detail view: its own local Player Perms/Player Groups sub-tabs. Owns
 * every piece of draft state for both (staged permission and group-membership additions/
 * removals) so it survives sub-tab switches - {@link AEUITabs} rebuilds each sub-tab's own
 * component from scratch every time the switch happens, so state living on THOSE instances would
 * be lost the moment the admin looked at the other tab. The sub-tabs are thin renderers over this
 * state; nothing is persisted until {@link #save()} is pressed - the same staged-then-Save
 * pattern already used by {@link AEUIGroupDetailsView}.
 */
public final class AEUIPlayerPermsDetailsView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Features/Permissions/AEUIPlayerPermsDetailsView.ui";

    private enum SubTab { PERMS, GROUPS }

    private final AEPlayer player;
    private final AEUITabs subTabs;

    private final LinkedHashSet<String> draftAddedPermissions = new LinkedHashSet<>();
    private final Set<String> draftRemovedPermissions = new LinkedHashSet<>();
    private final Map<String, AEUserPermissionMetadata> draftAddedPermissionMetadata = new HashMap<>();
    private final LinkedHashSet<String> draftAddedGroups = new LinkedHashSet<>();
    private final Set<String> draftRemovedGroups = new LinkedHashSet<>();
    private final Map<String, AEPlayerGroupMetadata> draftAddedGroupMetadata = new HashMap<>();

    public AEUIPlayerPermsDetailsView(AEPlayer player) {
        this.player = player;
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.playerperms.perms", SubTab.PERMS, () -> new AEUIPlayerPermsSubView(this)),
                new AEUITabDef("ui.tab.playerperms.groups", SubTab.GROUPS, () -> new AEUIPlayerGroupsSubView(this))
        ), SubTab.PERMS, false, this::onSubTabChanged);
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.Main", new AELabel(lang("ui.playerperms.title", args("player", getDisplayName(player)))),
                "Title.Id", List.of(),
                "SubTabs", subTabs,
                "Details", subTabs.current(),
                "Footer.SaveButton", new AEActionButton(lang("ui.common.save"), this::save),
                "Footer.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel)
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }

    AEPlayer getPlayer() {
        return player;
    }

    /**
     * Currently-granted permissions for this player plus staged additions - newest change first,
     * then their own grants newest-first. Staged removals stay in this list so their row keeps
     * rendering, red, until Save actually removes them. A staged addition hasn't been persisted, so
     * its {@code getCreatedAt()} is null; a persisted one is the real row already fetched here - no
     * follow-up query needed to show its Created At column.
     */
    List<AEPlayerPermission> getDisplayedPermissions() {
        LinkedHashMap<String, AEPlayerPermission> displayed = new LinkedHashMap<>();
        for (String node : draftAddedPermissions) {
            displayed.put(node, new AEPlayerPermission(0, player.getUuid(), node, null, null));
        }
        for (AEPlayerPermission relation : getPermissionsModule().getUsersModule().getDirectUserPermissionsOrderedByRecent(playerUuid())) {
            displayed.put(relation.getPermission(), relation);
        }
        return List.copyOf(displayed.values());
    }

    void addDraftPermission(String node) {
        if (node == null || node.isBlank()) {
            return;
        }
        draftRemovedPermissions.remove(node);
        draftAddedPermissions.add(node);
    }

    void removeDraftPermission(String node) {
        if (draftAddedPermissions.remove(node)) {
            draftAddedPermissionMetadata.remove(node);
        } else {
            draftRemovedPermissions.add(node);
        }
    }

    /** Undoes a staged removal - the row goes back to normal. No-op for anything else (a draft addition was already discarded outright by {@link #removeDraftPermission}, not staged as a removal). */
    void unremoveDraftPermission(String node) {
        draftRemovedPermissions.remove(node);
    }

    boolean isDraftAddedPermission(String node) {
        return draftAddedPermissions.contains(node);
    }

    boolean isDraftRemovedPermission(String node) {
        return draftRemovedPermissions.contains(node);
    }

    /**
     * Metadata can only be set while a permission is still a staged (not-yet-saved) addition - once
     * Save persists it, it's immutable; changing it means removing and re-adding the permission.
     */
    void stagePermissionMetadata(String node, String worldName, Timestamp expiresAt, String expiresInText) {
        if (!draftAddedPermissions.contains(node)) {
            return;
        }
        AEUserPermissionMetadata metadata = new AEUserPermissionMetadata(0, 0, worldName, expiresAt, null, null);
        metadata.setExpiresInText(expiresInText);
        draftAddedPermissionMetadata.put(node, metadata);
    }

    AEUserPermissionMetadata getStagedPermissionMetadata(String node) {
        return draftAddedPermissionMetadata.get(node);
    }

    /**
     * Currently-joined groups for this player plus staged additions, each paired with when the
     * membership was created (null for a staged addition, not yet persisted). Staged removals stay
     * in this map so their row keeps rendering, red, until Save actually removes them.
     */
    LinkedHashMap<String, Timestamp> getDisplayedGroups() {
        LinkedHashMap<String, Timestamp> displayed = new LinkedHashMap<>();
        for (String name : draftAddedGroups) {
            displayed.put(name, null);
        }
        displayed.putAll(getPermissionsModule().getUsersModule().getGroupsForUserOrderedByRecent(playerUuid()));
        return displayed;
    }

    void addDraftGroup(String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        draftRemovedGroups.remove(name);
        draftAddedGroups.add(name);
    }

    void removeDraftGroup(String name) {
        if (draftAddedGroups.remove(name)) {
            draftAddedGroupMetadata.remove(name);
        } else {
            draftRemovedGroups.add(name);
        }
    }

    /** Undoes a staged removal - the row goes back to normal. */
    void unremoveDraftGroup(String name) {
        draftRemovedGroups.remove(name);
    }

    boolean isDraftAddedGroup(String name) {
        return draftAddedGroups.contains(name);
    }

    boolean isDraftRemovedGroup(String name) {
        return draftRemovedGroups.contains(name);
    }

    /**
     * Metadata can only be set while a group membership is still a staged (not-yet-saved) addition -
     * once Save persists it, it's immutable; changing it means removing and re-adding the group.
     */
    void stageGroupMetadata(String name, String worldName, Timestamp expiresAt, String expiresInText) {
        if (!draftAddedGroups.contains(name)) {
            return;
        }
        AEPlayerGroupMetadata metadata = new AEPlayerGroupMetadata(0, 0, worldName, expiresAt, null, null);
        metadata.setExpiresInText(expiresInText);
        draftAddedGroupMetadata.put(name, metadata);
    }

    AEPlayerGroupMetadata getStagedGroupMetadata(String name) {
        return draftAddedGroupMetadata.get(name);
    }

    UUID playerUuid() {
        return UUID.fromString(player.getUuid());
    }

    AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }

    void cancel() {
        goBack();
    }

    void save() {
        AEPermissionsModule permissions = getPermissionsModule();
        AEExecutionContext context = getActorContext();
        AEActionResult last = null;
        if (!draftAddedPermissions.isEmpty()) {
            last = permissions.getUsersModule().handleAddUserPermissions(context, player.getUuid(), draftAddedPermissions);
            for (Map.Entry<String, AEUserPermissionMetadata> entry : draftAddedPermissionMetadata.entrySet()) {
                AEUserPermissionMetadata metadata = entry.getValue();
                permissions.getUsersModule().savePermissionMetadata(playerUuid(), entry.getKey(), metadata.getWorldName(), metadata.getExpiresAt());
            }
        }
        if (!draftRemovedPermissions.isEmpty()) {
            last = permissions.getUsersModule().handleRemoveUserPermissions(context, player.getUuid(), draftRemovedPermissions);
        }
        for (String group : draftAddedGroups) {
            last = permissions.getUsersModule().handleAddUserToGroup(context, player.getUuid(), group);
            AEPlayerGroupMetadata metadata = draftAddedGroupMetadata.get(group);
            if (metadata != null) {
                permissions.getUsersModule().saveGroupMetadata(playerUuid(), group, metadata.getWorldName(), metadata.getExpiresAt());
            }
        }
        for (String group : draftRemovedGroups) {
            last = permissions.getUsersModule().handleRemoveUserFromGroup(context, player.getUuid(), group);
        }
        if (last != null) {
            showNotification(last);
        }
        draftAddedPermissions.clear();
        draftRemovedPermissions.clear();
        draftAddedPermissionMetadata.clear();
        draftAddedGroups.clear();
        draftRemovedGroups.clear();
        draftAddedGroupMetadata.clear();
        refresh("Details");
    }

    private static String getDisplayName(AEPlayer player) {
        String name = player.getName();
        return name == null || name.isBlank() ? "Unknown" : name;
    }
}
