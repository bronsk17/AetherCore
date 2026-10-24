package me.bronsk.aethercore.ui.features.permissions;

import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEGroupSettingsModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermission;
import me.bronsk.aethercore.modules.permissions.models.AEGroupPermissionMetadata;
import me.bronsk.aethercore.modules.permissions.models.AEPlayerGroupMetadata;
import me.bronsk.aethercore.modules.player.models.AEPlayer;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.ui.AEUITabDef;
import me.bronsk.aethercore.ui.components.AEActionButton;
import me.bronsk.aethercore.ui.components.AEConfirmPopup;
import me.bronsk.aethercore.ui.components.AELabel;
import me.bronsk.aethercore.ui.components.AEUITabs;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * One group's detail view: its own local Perms/Children/Players sub-tabs - no top-level
 * Permissions sub-tabs here. Owns every piece of draft state for all three (name, staged
 * permission/child/player additions and removals) so it survives sub-tab switches - {@link
 * AEUITabs} rebuilds each sub-tab's own component from scratch every time the switch happens (its
 * supplier lambda runs again), so any state living on THOSE instances would be lost the moment the
 * admin looked at the other tab. The sub-tabs are thin renderers over this state, reading/writing
 * through package-private accessors and calling {@link #save()}/{@link #cancel()} for the shared
 * footer. Every edit is staged like this and only takes effect on Save - nothing is applied
 * immediately, whether the group already exists or is still being created via {@link
 * #createNew()}.
 */
public final class AEUIGroupDetailsView extends AEUIComponent {

    private static final String SHELL = "Pages/AetherCore/Features/Permissions/AEUIGroupDetailsView.ui";

    private enum SubTab { PERMS, CHILDREN, PLAYERS, SETTINGS }

    private AEGroup group;
    private final AEUITabs subTabs;

    private String draftName = "";
    private String draftPrefix;
    private String draftSuffix;
    private int draftPriority;
    private final LinkedHashSet<String> draftAddedPermissions = new LinkedHashSet<>();
    private final Set<String> draftRemovedPermissions = new LinkedHashSet<>();
    private final Map<String, AEGroupPermissionMetadata> draftAddedPermissionMetadata = new HashMap<>();
    private final LinkedHashSet<String> draftAddedChildren = new LinkedHashSet<>();
    private final Set<String> draftRemovedChildren = new LinkedHashSet<>();
    private final LinkedHashMap<String, AEPlayer> draftAddedPlayers = new LinkedHashMap<>();
    private final Set<String> draftRemovedPlayerUuids = new LinkedHashSet<>();
    private final Map<String, AEPlayerGroupMetadata> draftAddedPlayerMetadata = new HashMap<>();

    public static AEUIGroupDetailsView createNew() {
        return new AEUIGroupDetailsView(null);
    }

    public AEUIGroupDetailsView(AEGroup group) {
        this.group = group;
        if (group == null) {
            this.draftPrefix = "";
            this.draftSuffix = "";
            this.draftPriority = 0;
        } else {
            AEGroupSettingsModule settingsModule = getPermissionsModule().getGroupSettingsModule();
            this.draftPrefix = trimToEmpty(settingsModule.getGroupPrefix(group.getName()));
            this.draftSuffix = trimToEmpty(settingsModule.getGroupSuffix(group.getName()));
            Integer priority = settingsModule.getGroupPriority(group.getName());
            this.draftPriority = priority == null ? 0 : priority;
        }
        this.subTabs = new AEUITabs(List.of(
                new AEUITabDef("ui.tab.group.perms", SubTab.PERMS, () -> new AEUIGroupPermsSubView(this)),
                new AEUITabDef("ui.tab.group.children", SubTab.CHILDREN, () -> new AEUIGroupChildrenSubView(this)),
                new AEUITabDef("ui.tab.group.players", SubTab.PLAYERS, () -> new AEUIGroupPlayersSubView(this)),
                new AEUITabDef("ui.tab.group.settings", SubTab.SETTINGS, () -> new AEUIGroupSettingsSubView(this))
        ), SubTab.PERMS, false, this::onSubTabChanged);
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value;
    }

    @Override
    protected String getTemplate() {
        return SHELL;
    }

    @Override
    protected Map<String, Object> getContent() {
        return Map.of(
                "Title.Main", new AELabel(group == null ? lang("ui.group.title.new") : lang("ui.group.title.existing", args("group", group.getName()))),
                "Title.Id", List.of(),
                "SubTabs", subTabs,
                "Details", subTabs.current(),
                "Footer.DeleteButton", isCreateMode() ? List.of() : List.of(new AEActionButton(lang("ui.common.delete"), true, this::confirmDelete)),
                "Footer.SaveButton", new AEActionButton(lang("ui.common.save"), this::save),
                "Footer.CancelButton", new AEActionButton(lang("ui.common.cancel"), this::cancel)
        );
    }

    private void onSubTabChanged() {
        refresh("SubTabs", "Details");
    }

    boolean isCreateMode() {
        return group == null;
    }

    AEGroup getGroup() {
        return group;
    }

    String getDraftName() {
        return draftName;
    }

    void setDraftName(String value) {
        draftName = value;
    }

    String getDraftPrefix() {
        return draftPrefix;
    }

    void setDraftPrefix(String value) {
        draftPrefix = value;
    }

    String getDraftSuffix() {
        return draftSuffix;
    }

    void setDraftSuffix(String value) {
        draftSuffix = value;
    }

    int getDraftPriority() {
        return draftPriority;
    }

    void setDraftPriority(int value) {
        draftPriority = value;
    }

    /**
     * Currently-granted permissions for this group plus staged additions - newest change first,
     * then the group's own grants newest-first. Staged removals stay in this list (not filtered
     * out) so their row keeps rendering, red, until Save actually removes them; only Save clears
     * them from the underlying grant set they'd otherwise still be read from. A staged addition
     * hasn't been persisted, so its {@code getCreatedAt()} is null; a persisted one is the real row
     * already fetched here - no follow-up query needed to show its Created At column.
     */
    List<AEGroupPermission> getDisplayedPermissions() {
        LinkedHashMap<String, AEGroupPermission> displayed = new LinkedHashMap<>();
        for (String node : draftAddedPermissions) {
            displayed.put(node, new AEGroupPermission(0, 0, node, null, null));
        }
        if (!isCreateMode()) {
            for (AEGroupPermission relation : getPermissionsModule().getGroupsModule().getGroupPermissionsOrderedByRecent(group.getName())) {
                displayed.put(relation.getPermission(), relation);
            }
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
        AEGroupPermissionMetadata metadata = new AEGroupPermissionMetadata(0, 0, worldName, expiresAt, null, null);
        metadata.setExpiresInText(expiresInText);
        draftAddedPermissionMetadata.put(node, metadata);
    }

    AEGroupPermissionMetadata getStagedPermissionMetadata(String node) {
        return draftAddedPermissionMetadata.get(node);
    }

    /**
     * Groups this group currently inherits from plus staged additions, each paired with when the
     * inheritance edge was created (null for a staged addition, not yet persisted). Staged removals
     * stay in this map so their row keeps rendering, red, until Save actually removes them.
     */
    LinkedHashMap<String, Timestamp> getDisplayedChildren() {
        LinkedHashMap<String, Timestamp> displayed = new LinkedHashMap<>();
        for (String childName : draftAddedChildren) {
            displayed.put(childName, null);
        }
        if (!isCreateMode()) {
            displayed.putAll(getPermissionsModule().getGroupsModule().getGroupChildrenOrderedByRecent(group.getName()));
        }
        return displayed;
    }

    void addDraftChild(String childName) {
        if (childName == null || childName.isBlank()) {
            return;
        }
        draftRemovedChildren.remove(childName);
        draftAddedChildren.add(childName);
    }

    void removeDraftChild(String childName) {
        if (!draftAddedChildren.remove(childName)) {
            draftRemovedChildren.add(childName);
        }
    }

    /** Undoes a staged removal - the row goes back to normal. */
    void unremoveDraftChild(String childName) {
        draftRemovedChildren.remove(childName);
    }

    boolean isDraftAddedChild(String childName) {
        return draftAddedChildren.contains(childName);
    }

    boolean isDraftRemovedChild(String childName) {
        return draftRemovedChildren.contains(childName);
    }

    /**
     * Current members of this group plus staged additions, each paired with when their membership
     * was created (null for a staged addition, not yet persisted) - keyed by UUID (not the {@link
     * AEPlayer} instance, which has no {@code equals}/{@code hashCode} of its own). Staged removals
     * stay in this map so their row keeps rendering, red, until Save actually removes them.
     */
    LinkedHashMap<String, Map.Entry<AEPlayer, Timestamp>> getDisplayedPlayers() {
        LinkedHashMap<String, Map.Entry<AEPlayer, Timestamp>> displayed = new LinkedHashMap<>();
        for (AEPlayer player : draftAddedPlayers.values()) {
            displayed.put(player.getUuid(), new AbstractMap.SimpleEntry<>(player, null));
        }
        if (!isCreateMode()) {
            displayed.putAll(getPermissionsModule().getGroupsModule().getPlayersInGroup(group.getName()));
        }
        return displayed;
    }

    void addDraftPlayer(AEPlayer player) {
        draftRemovedPlayerUuids.remove(player.getUuid());
        draftAddedPlayers.put(player.getUuid(), player);
    }

    void removeDraftPlayer(String playerUuid) {
        if (draftAddedPlayers.remove(playerUuid) != null) {
            draftAddedPlayerMetadata.remove(playerUuid);
        } else {
            draftRemovedPlayerUuids.add(playerUuid);
        }
    }

    /** Undoes a staged removal - the row goes back to normal. */
    void unremoveDraftPlayer(String playerUuid) {
        draftRemovedPlayerUuids.remove(playerUuid);
    }

    boolean isDraftAddedPlayer(String playerUuid) {
        return draftAddedPlayers.containsKey(playerUuid);
    }

    boolean isDraftRemovedPlayer(String playerUuid) {
        return draftRemovedPlayerUuids.contains(playerUuid);
    }

    /**
     * Metadata can only be set while a player's membership is still a staged (not-yet-saved)
     * addition - once Save persists it, it's immutable; changing it means removing and re-adding
     * the player.
     */
    void stagePlayerMetadata(String playerUuid, String worldName, Timestamp expiresAt, String expiresInText) {
        if (!draftAddedPlayers.containsKey(playerUuid)) {
            return;
        }
        AEPlayerGroupMetadata metadata = new AEPlayerGroupMetadata(0, 0, worldName, expiresAt, null, null);
        metadata.setExpiresInText(expiresInText);
        draftAddedPlayerMetadata.put(playerUuid, metadata);
    }

    AEPlayerGroupMetadata getStagedPlayerMetadata(String playerUuid) {
        return draftAddedPlayerMetadata.get(playerUuid);
    }

    void cancel() {
        goBack();
    }

    private void confirmDelete() {
        showPopup(new AEConfirmPopup(
                lang("ui.group.confirm_delete.title", args("group", group.getName())),
                lang("ui.group.confirm_delete.message"),
                this::deleteGroup
        ));
    }

    private void deleteGroup() {
        AEActionResult result = getPermissionsModule().getGroupsModule().handleDeleteGroup(getActorContext(), group.getName());
        showNotification(result);
        if (result.success()) {
            goBack();
        }
    }

    void save() {
        if (isCreateMode()) {
            saveNew();
        } else {
            saveExisting();
        }
    }

    private void saveNew() {
        if (draftName.isBlank()) {
            showNotification(AEActionResult.error(AEMessageUtils.fromColoredString(lang("ui.group.name_required"))));
            return;
        }
        AEPermissionsModule permissions = getPermissionsModule();
        AEExecutionContext context = getActorContext();
        AEActionResult created = permissions.getGroupsModule().handleCreateGroup(context, draftName);
        if (!created.success()) {
            showNotification(created);
            return;
        }
        AEActionResult applied = applyDraftChanges(permissions, context, draftName);
        showNotification(applied != null ? applied : created);
        navigate(new AEUIGroupDetailsView(permissions.getGroupsModule().getGroup(draftName)));
    }

    private void saveExisting() {
        AEPermissionsModule permissions = getPermissionsModule();
        AEActionResult result = applyDraftChanges(permissions, getActorContext(), group.getName());
        if (result != null) {
            showNotification(result);
        }
        group = permissions.getGroupsModule().getGroup(group.getName());
        draftAddedPermissions.clear();
        draftRemovedPermissions.clear();
        draftAddedPermissionMetadata.clear();
        draftAddedChildren.clear();
        draftRemovedChildren.clear();
        draftAddedPlayers.clear();
        draftRemovedPlayerUuids.clear();
        draftAddedPlayerMetadata.clear();
        refresh("Details");
    }

    private AEActionResult applyDraftChanges(AEPermissionsModule permissions, AEExecutionContext context, String groupName) {
        AEActionResult last = null;
        if (!draftAddedPermissions.isEmpty()) {
            last = permissions.getGroupsModule().handleAddGroupPermissions(context, groupName, draftAddedPermissions);
            for (Map.Entry<String, AEGroupPermissionMetadata> entry : draftAddedPermissionMetadata.entrySet()) {
                AEGroupPermissionMetadata metadata = entry.getValue();
                permissions.getGroupsModule().savePermissionMetadata(groupName, entry.getKey(), metadata.getWorldName(), metadata.getExpiresAt());
            }
        }
        if (!draftRemovedPermissions.isEmpty()) {
            last = permissions.getGroupsModule().handleRemoveGroupPermissions(context, groupName, draftRemovedPermissions);
        }
        for (String child : draftAddedChildren) {
            last = permissions.getGroupsModule().handleAddGroupChild(context, groupName, child);
        }
        for (String child : draftRemovedChildren) {
            last = permissions.getGroupsModule().handleRemoveGroupChild(context, groupName, child);
        }
        for (AEPlayer player : draftAddedPlayers.values()) {
            last = permissions.getUsersModule().handleAddUserToGroup(context, player.getUuid(), groupName);
            AEPlayerGroupMetadata metadata = draftAddedPlayerMetadata.get(player.getUuid());
            if (metadata != null) {
                permissions.getUsersModule().saveGroupMetadata(UUID.fromString(player.getUuid()), groupName, metadata.getWorldName(), metadata.getExpiresAt());
            }
        }
        for (String playerUuid : draftRemovedPlayerUuids) {
            last = permissions.getUsersModule().handleRemoveUserFromGroup(context, playerUuid, groupName);
        }
        last = permissions.getGroupSettingsModule().handleSetGroupPrefix(context, groupName, draftPrefix);
        last = permissions.getGroupSettingsModule().handleSetGroupSuffix(context, groupName, draftSuffix);
        last = permissions.getGroupSettingsModule().handleSetGroupPriority(context, groupName, draftPriority);
        return last;
    }

    private AEPermissionsModule getPermissionsModule() {
        return AEModulesManager.get().getModule(AEPermissionsModule.class);
    }
}
