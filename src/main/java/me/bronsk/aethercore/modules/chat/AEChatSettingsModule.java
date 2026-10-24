package me.bronsk.aethercore.modules.chat;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.chat.models.AEChatGroupSettings;
import me.bronsk.aethercore.modules.chat.models.AEChatPlayerSettings;
import me.bronsk.aethercore.modules.chat.models.AEChatSettings;
import me.bronsk.aethercore.modules.chat.storage.AEChatStorage;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.AEPlayersModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * Owns the 6 chat settings (format, server/player join message, leave message, first-join message,
 * broadcast-first-join) for both groups and players, and the group-priority/parent-chain resolution
 * shared between them.
 */
public class AEChatSettingsModule extends AESubModule<AEChatModule, AEChatStorage> {

    @Override
    public String getName() {
        return "settings";
    }

    /*
        FORMAT
     */
    public Message handleGetGroupFormat(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "format", AEChatSettings::getFormat, getDefaultFormat());
    }

    public Message handleSetGroupFormat(AEExecutionContext context, String groupName, String value) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        String normalized = normalizeFormatValue(value);
        if (normalized == null) {
            return getInvalidFormatMessage(context, "format", value);
        }
        saveGroupSetting(groupName, settings -> settings.setFormat(normalized));
        return getSettingMessage(context, "chat.group-setting.set.success", "format", groupName, normalized);
    }

    public Message handleResetGroupFormat(AEExecutionContext context, String groupName) {
        return resetGroupStringSetting(context, groupName, "format", AEChatGroupSettings::setFormat, AEChatSettings::getFormat, getDefaultFormat());
    }

    public Message handleGetPlayerFormat(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "format", AEChatSettings::getFormat, getDefaultFormat());
    }

    public Message handleSetPlayerFormat(AEExecutionContext context, String player, String value) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        String normalized = normalizeFormatValue(value);
        if (normalized == null) {
            return getInvalidFormatMessage(context, "format", value);
        }
        savePlayerSetting(uuid, settings -> settings.setFormat(normalized));
        return getSettingMessage(context, "chat.player-setting.set.success", "format", player, normalized);
    }

    public Message handleResetPlayerFormat(AEExecutionContext context, String player) {
        return resetPlayerStringSetting(context, player, "format", AEChatPlayerSettings::setFormat, AEChatSettings::getFormat, getDefaultFormat());
    }

    /*
        SERVER JOIN MESSAGE
     */
    public Message handleGetGroupServerJoinMessage(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "server_join_message", AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode());
    }

    public Message handleSetGroupServerJoinMessage(AEExecutionContext context, String groupName, String value) {
        return setGroupStringSetting(context, groupName, "server_join_message", value, AEChatGroupSettings::setServerJoinMessageCode);
    }

    public Message handleResetGroupServerJoinMessage(AEExecutionContext context, String groupName) {
        return resetGroupStringSetting(context, groupName, "server_join_message", AEChatGroupSettings::setServerJoinMessageCode, AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode());
    }

    public Message handleGetPlayerServerJoinMessage(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "server_join_message", AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode());
    }

    public Message handleSetPlayerServerJoinMessage(AEExecutionContext context, String player, String value) {
        return setPlayerStringSetting(context, player, "server_join_message", value, AEChatPlayerSettings::setServerJoinMessageCode);
    }

    public Message handleResetPlayerServerJoinMessage(AEExecutionContext context, String player) {
        return resetPlayerStringSetting(context, player, "server_join_message", AEChatPlayerSettings::setServerJoinMessageCode, AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode());
    }

    /*
        PLAYER JOIN MESSAGE
     */
    public Message handleGetGroupPlayerJoinMessage(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "player_join_message", AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode());
    }

    public Message handleSetGroupPlayerJoinMessage(AEExecutionContext context, String groupName, String value) {
        return setGroupStringSetting(context, groupName, "player_join_message", value, AEChatGroupSettings::setPlayerJoinMessageCode);
    }

    public Message handleResetGroupPlayerJoinMessage(AEExecutionContext context, String groupName) {
        return resetGroupStringSetting(context, groupName, "player_join_message", AEChatGroupSettings::setPlayerJoinMessageCode, AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode());
    }

    public Message handleGetPlayerPlayerJoinMessage(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "player_join_message", AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode());
    }

    public Message handleSetPlayerPlayerJoinMessage(AEExecutionContext context, String player, String value) {
        return setPlayerStringSetting(context, player, "player_join_message", value, AEChatPlayerSettings::setPlayerJoinMessageCode);
    }

    public Message handleResetPlayerPlayerJoinMessage(AEExecutionContext context, String player) {
        return resetPlayerStringSetting(context, player, "player_join_message", AEChatPlayerSettings::setPlayerJoinMessageCode, AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode());
    }

    /*
        LEAVE MESSAGE
     */
    public Message handleGetGroupLeaveMessage(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "leave_message", AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode());
    }

    public Message handleSetGroupLeaveMessage(AEExecutionContext context, String groupName, String value) {
        return setGroupStringSetting(context, groupName, "leave_message", value, AEChatGroupSettings::setLeaveMessageCode);
    }

    public Message handleResetGroupLeaveMessage(AEExecutionContext context, String groupName) {
        return resetGroupStringSetting(context, groupName, "leave_message", AEChatGroupSettings::setLeaveMessageCode, AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode());
    }

    public Message handleGetPlayerLeaveMessage(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "leave_message", AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode());
    }

    public Message handleSetPlayerLeaveMessage(AEExecutionContext context, String player, String value) {
        return setPlayerStringSetting(context, player, "leave_message", value, AEChatPlayerSettings::setLeaveMessageCode);
    }

    public Message handleResetPlayerLeaveMessage(AEExecutionContext context, String player) {
        return resetPlayerStringSetting(context, player, "leave_message", AEChatPlayerSettings::setLeaveMessageCode, AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode());
    }

    /*
        FIRST JOIN MESSAGE
     */
    public Message handleGetGroupFirstJoinMessage(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "first_join_message", AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode());
    }

    public Message handleSetGroupFirstJoinMessage(AEExecutionContext context, String groupName, String value) {
        return setGroupStringSetting(context, groupName, "first_join_message", value, AEChatGroupSettings::setFirstJoinMessageCode);
    }

    public Message handleResetGroupFirstJoinMessage(AEExecutionContext context, String groupName) {
        return resetGroupStringSetting(context, groupName, "first_join_message", AEChatGroupSettings::setFirstJoinMessageCode, AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode());
    }

    public Message handleGetPlayerFirstJoinMessage(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "first_join_message", AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode());
    }

    public Message handleSetPlayerFirstJoinMessage(AEExecutionContext context, String player, String value) {
        return setPlayerStringSetting(context, player, "first_join_message", value, AEChatPlayerSettings::setFirstJoinMessageCode);
    }

    public Message handleResetPlayerFirstJoinMessage(AEExecutionContext context, String player) {
        return resetPlayerStringSetting(context, player, "first_join_message", AEChatPlayerSettings::setFirstJoinMessageCode, AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode());
    }

    /*
        BROADCAST FIRST JOIN MESSAGE
     */
    public Message handleGetGroupBroadcastFirstJoinMessage(AEExecutionContext context, String groupName) {
        return getGroupSettingMessage(context, groupName, "broadcast_first_join_message", AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage());
    }

    public Message handleSetGroupBroadcastFirstJoinMessage(AEExecutionContext context, String groupName, boolean value) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        saveGroupSetting(groupName, settings -> settings.setBroadcastFirstJoinMessage(value));
        return getSettingMessage(context, "chat.group-setting.set.success", "broadcast_first_join_message", groupName, String.valueOf(value));
    }

    public Message handleResetGroupBroadcastFirstJoinMessage(AEExecutionContext context, String groupName) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        resetGroupSetting(groupName, settings -> settings.setBroadcastFirstJoinMessage(null));
        return getGroupSettingMessage(context, "chat.group-setting.reset.success", groupName, "broadcast_first_join_message", AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage());
    }

    public Message handleGetPlayerBroadcastFirstJoinMessage(AEExecutionContext context, String player) {
        return getPlayerSettingMessage(context, player, "broadcast_first_join_message", AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage());
    }

    public Message handleSetPlayerBroadcastFirstJoinMessage(AEExecutionContext context, String player, boolean value) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        savePlayerSetting(uuid, settings -> settings.setBroadcastFirstJoinMessage(value));
        return getSettingMessage(context, "chat.player-setting.set.success", "broadcast_first_join_message", player, String.valueOf(value));
    }

    public Message handleResetPlayerBroadcastFirstJoinMessage(AEExecutionContext context, String player) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        resetPlayerSetting(uuid, settings -> settings.setBroadcastFirstJoinMessage(null));
        String value = getSettingForPlayerOrGroups(uuid, AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage(), getGroupsForUser(uuid));
        return getSettingMessage(context, "chat.player-setting.reset.success", "broadcast_first_join_message", player, value);
    }

    /*
        RESOLUTION ENGINE - also used by AEChatModule for the live chat/join/leave messages
     */

    /** The player's own direct value, else the value from their highest-priority group (falling back to any group's inherited value), else {@code defaultValue}. */
    public String getSettingForPlayerOrGroups(UUID playerUuid, Function<AEChatSettings, String> getter, String defaultValue, Set<String> groupNames) {
        AEChatPlayerSettings playerSettings = AEDatabaseUtils.supplyUnchecked(
                () -> getStorage().getChatPlayerSettings(playerUuid.toString()));
        if (playerSettings != null && getter.apply(playerSettings) != null) {
            return getter.apply(playerSettings);
        }

        List<String> values = getResolvedGroupSettingValues(getter, groupNames);
        if (!values.isEmpty()) {
            return values.getFirst();
        }
        return defaultValue;
    }

    public Set<String> getGroupsForUser(UUID uuid) {
        return getPermissionsModule().getUsersModule().getGroupsForUser(uuid);
    }

    /** Direct value for this group, else its nearest ancestor with a direct value, else {@code defaultValue}. */
    private String resolveGroupSettingValue(String groupName, Function<AEChatSettings, String> getter, String defaultValue) {
        String direct = getDirectGroupSettingValue(groupName, getter);
        if (direct != null) {
            return direct;
        }

        for (String childGroup : getPermissionsModule().getGroupsModule().getTransitiveGroupChildren(groupName)) {
            String childValue = getDirectGroupSettingValue(childGroup, getter);
            if (childValue != null) {
                return childValue;
            }
        }

        return defaultValue;
    }

    private String getDirectGroupSettingValue(String groupName, Function<AEChatSettings, String> getter) {
        AEChatGroupSettings settings = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getChatGroupSettings(groupName));
        return settings == null ? null : getter.apply(settings);
    }

    private List<String> getResolvedGroupSettingValues(Function<AEChatSettings, String> getter, Set<String> groupNames) {
        List<String> directValues = getHighestPriorityGroupSettingValues(getter, groupNames);
        if (!directValues.isEmpty()) {
            return directValues;
        }
        return getInheritedGroupSettingValues(getter, groupNames);
    }

    private List<String> getHighestPriorityGroupSettingValues(Function<AEChatSettings, String> getter, Set<String> groupNames) {
        List<String> settingValues = new ArrayList<>();
        Integer highestPriority = null;

        for (String groupName : groupNames) {
            Integer priority = getPermissionsModule().getGroupSettingsModule().getGroupPriority(groupName);
            String value = getDirectGroupSettingValue(groupName, getter);
            if (priority == null || value == null) {
                continue;
            }

            if (highestPriority == null || priority > highestPriority) {
                highestPriority = priority;
                settingValues.clear();
                settingValues.add(value);
                continue;
            }

            if (priority.equals(highestPriority)) {
                settingValues.add(value);
            }
        }

        return settingValues;
    }

    private List<String> getInheritedGroupSettingValues(Function<AEChatSettings, String> getter, Set<String> groupNames) {
        for (String groupName : groupNames) {
            for (String childGroup : getPermissionsModule().getGroupsModule().getTransitiveGroupChildren(groupName)) {
                String value = getDirectGroupSettingValue(childGroup, getter);
                if (value != null) {
                    return List.of(value);
                }
            }
        }
        return List.of();
    }

    /*
        ADMIN UI SUPPORT
     */

    /** The group's 6 settings resolved to their current effective values (own override, else inherited, else default) - pre-fills the admin panel's settings form. */
    public AEChatGroupSettings getEffectiveGroupSettings(String groupName) {
        AEChatGroupSettings settings = new AEChatGroupSettings();
        settings.setFormat(resolveGroupSettingValue(groupName, AEChatSettings::getFormat, getDefaultFormat()));
        settings.setServerJoinMessageCode(resolveGroupSettingValue(groupName, AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode()));
        settings.setPlayerJoinMessageCode(resolveGroupSettingValue(groupName, AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode()));
        settings.setLeaveMessageCode(resolveGroupSettingValue(groupName, AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode()));
        settings.setFirstJoinMessageCode(resolveGroupSettingValue(groupName, AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode()));
        String broadcast = resolveGroupSettingValue(groupName, AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage());
        settings.setBroadcastFirstJoinMessage(Boolean.valueOf(broadcast));
        return settings;
    }

    /** Same as {@link #getEffectiveGroupSettings} but for a player's direct-or-inherited-from-groups settings. */
    public AEChatPlayerSettings getEffectivePlayerSettings(UUID uuid) {
        Set<String> groupNames = getGroupsForUser(uuid);
        AEChatPlayerSettings settings = new AEChatPlayerSettings();
        settings.setFormat(getSettingForPlayerOrGroups(uuid, AEChatSettings::getFormat, getDefaultFormat(), groupNames));
        settings.setServerJoinMessageCode(getSettingForPlayerOrGroups(uuid, AEChatSettings::getServerJoinMessageCode, getDefaultServerJoinMessageCode(), groupNames));
        settings.setPlayerJoinMessageCode(getSettingForPlayerOrGroups(uuid, AEChatSettings::getPlayerJoinMessageCode, getDefaultPlayerJoinMessageCode(), groupNames));
        settings.setLeaveMessageCode(getSettingForPlayerOrGroups(uuid, AEChatSettings::getLeaveMessageCode, getDefaultLeaveMessageCode(), groupNames));
        settings.setFirstJoinMessageCode(getSettingForPlayerOrGroups(uuid, AEChatSettings::getFirstJoinMessageCode, getDefaultFirstJoinMessageCode(), groupNames));
        String broadcast = getSettingForPlayerOrGroups(uuid, AEChatSettingsModule::formatBroadcast, getDefaultBroadcastFirstJoinMessage(), groupNames);
        settings.setBroadcastFirstJoinMessage(Boolean.valueOf(broadcast));
        return settings;
    }

    /** Saves all 6 settings for a group at once - used by the admin panel's single Save button. */
    public AEActionResult handleSaveGroupSettings(AEExecutionContext context, String groupName, AEChatGroupSettings form) {
        if (!isKnownGroup(groupName)) {
            return AEActionResult.error(getMissingGroupMessage(context, groupName));
        }
        String normalizedFormat = normalizeFormatValue(form.getFormat());
        if (normalizedFormat == null) {
            return AEActionResult.error(getInvalidFormatMessage(context, "format", form.getFormat()));
        }
        String serverJoin = normalizeLength(form.getServerJoinMessageCode());
        String playerJoin = normalizeLength(form.getPlayerJoinMessageCode());
        String leave = normalizeLength(form.getLeaveMessageCode());
        String firstJoin = normalizeLength(form.getFirstJoinMessageCode());
        if (serverJoin == null || playerJoin == null || leave == null || firstJoin == null) {
            return AEActionResult.error(getInvalidLengthMessage(context, "message"));
        }

        saveGroupSetting(groupName, settings -> {
            settings.setFormat(normalizedFormat);
            settings.setServerJoinMessageCode(serverJoin);
            settings.setPlayerJoinMessageCode(playerJoin);
            settings.setLeaveMessageCode(leave);
            settings.setFirstJoinMessageCode(firstJoin);
            settings.setBroadcastFirstJoinMessage(form.getBroadcastFirstJoinMessage());
        });
        return AEActionResult.success(Message.raw("Chat settings for group '" + groupName + "' saved."));
    }

    /** Saves all 6 settings for a player at once - used by the admin panel's single Save button. */
    public AEActionResult handleSavePlayerSettings(AEExecutionContext context, UUID uuid, String playerLabel, AEChatPlayerSettings form) {
        String normalizedFormat = normalizeFormatValue(form.getFormat());
        if (normalizedFormat == null) {
            return AEActionResult.error(getInvalidFormatMessage(context, "format", form.getFormat()));
        }
        String serverJoin = normalizeLength(form.getServerJoinMessageCode());
        String playerJoin = normalizeLength(form.getPlayerJoinMessageCode());
        String leave = normalizeLength(form.getLeaveMessageCode());
        String firstJoin = normalizeLength(form.getFirstJoinMessageCode());
        if (serverJoin == null || playerJoin == null || leave == null || firstJoin == null) {
            return AEActionResult.error(getInvalidLengthMessage(context, "message"));
        }

        savePlayerSetting(uuid, settings -> {
            settings.setFormat(normalizedFormat);
            settings.setServerJoinMessageCode(serverJoin);
            settings.setPlayerJoinMessageCode(playerJoin);
            settings.setLeaveMessageCode(leave);
            settings.setFirstJoinMessageCode(firstJoin);
            settings.setBroadcastFirstJoinMessage(form.getBroadcastFirstJoinMessage());
        });
        return AEActionResult.success(Message.raw("Chat settings for player '" + playerLabel + "' saved."));
    }

    /*
        COMMAND HANDLER HELPERS
     */

    private Message setGroupStringSetting(AEExecutionContext context, String groupName, String key, String value, BiConsumer<AEChatGroupSettings, String> setter) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        String normalized = normalizeLength(value);
        if (normalized == null) {
            return getInvalidLengthMessage(context, key);
        }
        saveGroupSetting(groupName, settings -> setter.accept(settings, normalized));
        return getSettingMessage(context, "chat.group-setting.set.success", key, groupName, normalized);
    }

    private Message resetGroupStringSetting(AEExecutionContext context, String groupName, String key,
                                             BiConsumer<AEChatGroupSettings, String> setter,
                                             Function<AEChatSettings, String> getter, String defaultValue) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        resetGroupSetting(groupName, settings -> setter.accept(settings, null));
        return getGroupSettingMessage(context, "chat.group-setting.reset.success", groupName, key, getter, defaultValue);
    }

    private Message setPlayerStringSetting(AEExecutionContext context, String player, String key, String value, BiConsumer<AEChatPlayerSettings, String> setter) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        String normalized = normalizeLength(value);
        if (normalized == null) {
            return getInvalidLengthMessage(context, key);
        }
        savePlayerSetting(uuid, settings -> setter.accept(settings, normalized));
        return getSettingMessage(context, "chat.player-setting.set.success", key, player, normalized);
    }

    private Message resetPlayerStringSetting(AEExecutionContext context, String player, String key,
                                              BiConsumer<AEChatPlayerSettings, String> setter,
                                              Function<AEChatSettings, String> getter, String defaultValue) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        resetPlayerSetting(uuid, settings -> setter.accept(settings, null));
        String value = getSettingForPlayerOrGroups(uuid, getter, defaultValue, getGroupsForUser(uuid));
        return getSettingMessage(context, "chat.player-setting.reset.success", key, player, value);
    }

    private Message getGroupSettingMessage(AEExecutionContext context, String groupName, String key, Function<AEChatSettings, String> getter, String defaultValue) {
        return getGroupSettingMessage(context, "chat.group-setting.get.success", groupName, key, getter, defaultValue);
    }

    private Message getGroupSettingMessage(AEExecutionContext context, String langKey, String groupName, String key, Function<AEChatSettings, String> getter, String defaultValue) {
        if (!isKnownGroup(groupName)) {
            return getMissingGroupMessage(context, groupName);
        }
        String value = resolveGroupSettingValue(groupName, getter, defaultValue);
        return getSettingMessage(context, langKey, key, groupName, value);
    }

    private Message getPlayerSettingMessage(AEExecutionContext context, String player, String key, Function<AEChatSettings, String> getter, String defaultValue) {
        UUID uuid = players().resolvePlayerUuid(player);
        if (uuid == null) {
            return getMissingPlayerMessage(context, player);
        }
        String value = getSettingForPlayerOrGroups(uuid, getter, defaultValue, getGroupsForUser(uuid));
        return getSettingMessage(context, "chat.player-setting.get.success", key, player, value);
    }

    private Message getSettingMessage(AEExecutionContext context, String langKey, String key, String target, String value) {
        return getLangModule().buildMessage(langKey, context,
                args("setting", key, "group", target, "target", target, "value", formatSettingValue(value)));
    }

    private void saveGroupSetting(String groupName, Consumer<AEChatGroupSettings> mutator) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEChatGroupSettings settings = getStorage().getOrCreateChatGroupSettings(groupName);
            mutator.accept(settings);
            getStorage().saveChatGroupSettings(settings);
        });
    }

    private void resetGroupSetting(String groupName, Consumer<AEChatGroupSettings> mutator) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEChatGroupSettings settings = getStorage().getChatGroupSettings(groupName);
            if (settings == null) {
                return;
            }
            mutator.accept(settings);
            getStorage().saveChatGroupSettings(settings);
        });
    }

    private void savePlayerSetting(UUID uuid, Consumer<AEChatPlayerSettings> mutator) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEChatPlayerSettings settings = getStorage().getOrCreateChatPlayerSettings(uuid.toString());
            mutator.accept(settings);
            getStorage().saveChatPlayerSettings(settings);
        });
    }

    private void resetPlayerSetting(UUID uuid, Consumer<AEChatPlayerSettings> mutator) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEChatPlayerSettings settings = getStorage().getChatPlayerSettings(uuid.toString());
            if (settings == null) {
                return;
            }
            mutator.accept(settings);
            getStorage().saveChatPlayerSettings(settings);
        });
    }

    private boolean isKnownGroup(String groupName) {
        return PermissionsModule.get().getAllRegisteredGroups().contains(groupName);
    }

    private String normalizeLength(String value) {
        String normalized = value == null ? "" : value;
        int length = normalized.length();
        return length > 256 ? null : normalized;
    }

    private String normalizeFormatValue(String value) {
        String normalized = value == null ? "" : value;
        if (normalized.isEmpty() || normalized.length() > 256) {
            return null;
        }
        if (!normalized.contains("{player}") || !normalized.contains("{message}")) {
            return null;
        }
        return normalized;
    }

    private static String formatBroadcast(AEChatSettings settings) {
        Boolean value = settings.getBroadcastFirstJoinMessage();
        return value == null ? null : String.valueOf(value);
    }

    private String formatSettingValue(String value) {
        return value == null ? "null" : value;
    }

    private String getDefaultFormat() {
        return getParentModule().getConfig().getDefaultFormat();
    }

    private String getDefaultServerJoinMessageCode() {
        return getParentModule().getConfig().getDefaultServerJoinMessageCode();
    }

    private String getDefaultPlayerJoinMessageCode() {
        return getParentModule().getConfig().getDefaultPlayerJoinMessageCode();
    }

    private String getDefaultLeaveMessageCode() {
        return getParentModule().getConfig().getDefaultLeaveMessageCode();
    }

    private String getDefaultFirstJoinMessageCode() {
        return getParentModule().getConfig().getDefaultFirstJoinMessageCode();
    }

    private String getDefaultBroadcastFirstJoinMessage() {
        Boolean value = getParentModule().getConfig().getDefaultBroadcastFirstJoinMessage();
        return value == null ? null : String.valueOf(value);
    }

    private Message getMissingGroupMessage(AEExecutionContext context, String groupName) {
        return getLangModule().buildMessage("chat.group-setting.group.missing", context, args("group", groupName));
    }

    private Message getMissingPlayerMessage(AEExecutionContext context, String player) {
        return getLangModule().buildMessage("chat.player-setting.player.missing", context, args("target", player));
    }

    private Message getInvalidLengthMessage(AEExecutionContext context, String key) {
        return getLangModule().buildMessage("chat.setting.invalid", context, args("setting", key, "min", "0", "max", "256"));
    }

    private Message getInvalidFormatMessage(AEExecutionContext context, String key, String value) {
        return getLangModule().buildMessage("chat.setting.format.invalid", context,
                args("setting", key, "value", value == null ? "" : value));
    }

    private AEPermissionsModule getPermissionsModule() {
        return getModulesManager().getModule(AEPermissionsModule.class);
    }

    private AEPlayersModule players() {
        return getModulesManager().getModule(AEPlayersModule.class);
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
