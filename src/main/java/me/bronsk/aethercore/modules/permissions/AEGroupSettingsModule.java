package me.bronsk.aethercore.modules.permissions;

import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AESubModule;
import me.bronsk.aethercore.modules.permissions.models.AEGroup;
import me.bronsk.aethercore.modules.permissions.models.AEGroupSettings;
import me.bronsk.aethercore.modules.permissions.storage.AEPermissionsStorage;

import java.util.UUID;
import java.util.function.Consumer;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/** Owns per-group settings (prefix, suffix, priority) and the player prefix/suffix resolved from them. */
public class AEGroupSettingsModule extends AESubModule<AEPermissionsModule, AEPermissionsStorage> {

    private static final int NAME_MIN_LENGTH = 0;
    private static final int NAME_MAX_LENGTH = 256;

    @Override
    public String getName() {
        return "settings";
    }

    public Message handleGetGroupPrefix(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        return getSettingMessage(context, "permissions.group.setting.get.success", "prefix", groupName, getPrefixValue(groupName));
    }

    public AEActionResult handleSetGroupPrefix(AEExecutionContext context, String groupName, String value) {
        if (getGroup(groupName) == null) {
            return AEActionResult.error(getMissingGroupMessage(context, groupName));
        }
        String normalized = normalizeLength(value);
        if (normalized == null) {
            return AEActionResult.error(getInvalidLengthMessage(context, "prefix"));
        }
        saveGroupSettings(groupName, settings -> settings.setPrefix(normalized));
        return AEActionResult.success(getSettingMessage(context, "permissions.group.setting.set.success", "prefix", groupName, normalized));
    }

    public Message handleResetGroupPrefix(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        saveGroupSettings(groupName, settings -> settings.setPrefix(null));
        return getSettingMessage(context, "permissions.group.setting.reset.success", "prefix", groupName, getPrefixValue(groupName));
    }

    public Message handleGetGroupSuffix(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        return getSettingMessage(context, "permissions.group.setting.get.success", "suffix", groupName, getSuffixValue(groupName));
    }

    public AEActionResult handleSetGroupSuffix(AEExecutionContext context, String groupName, String value) {
        if (getGroup(groupName) == null) {
            return AEActionResult.error(getMissingGroupMessage(context, groupName));
        }
        String normalized = normalizeLength(value);
        if (normalized == null) {
            return AEActionResult.error(getInvalidLengthMessage(context, "suffix"));
        }
        saveGroupSettings(groupName, settings -> settings.setSuffix(normalized));
        return AEActionResult.success(getSettingMessage(context, "permissions.group.setting.set.success", "suffix", groupName, normalized));
    }

    public Message handleResetGroupSuffix(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        saveGroupSettings(groupName, settings -> settings.setSuffix(null));
        return getSettingMessage(context, "permissions.group.setting.reset.success", "suffix", groupName, getSuffixValue(groupName));
    }

    public Message handleGetGroupPriority(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        return getSettingMessage(context, "permissions.group.setting.get.success", "priority", groupName, String.valueOf(getGroupPriority(groupName)));
    }

    public AEActionResult handleSetGroupPriority(AEExecutionContext context, String groupName, int value) {
        if (getGroup(groupName) == null) {
            return AEActionResult.error(getMissingGroupMessage(context, groupName));
        }
        saveGroupSettings(groupName, settings -> settings.setPriority(value));
        return AEActionResult.success(getSettingMessage(context, "permissions.group.setting.set.success", "priority", groupName, String.valueOf(value)));
    }

    public Message handleResetGroupPriority(AEExecutionContext context, String groupName) {
        if (getGroup(groupName) == null) {
            return getMissingGroupMessage(context, groupName);
        }
        saveGroupSettings(groupName, settings -> settings.setPriority(0));
        return getSettingMessage(context, "permissions.group.setting.reset.success", "priority", groupName, "0");
    }

    /**
     * Returns the priority used to order inherited group settings.
     *
     * @param groupName Group name.
     * @return Group priority, or null when the group is unknown.
     */
    public Integer getGroupPriority(String groupName) {
        if (getGroup(groupName) == null) {
            return null;
        }

        AEGroupSettings settings = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupSettings(groupName));
        return settings == null ? 0 : settings.getPriority();
    }

    public String getPlayerPrefix(UUID uuid) {
        return getFirstPlayerGroupValue(uuid, AEGroupSettings::getPrefix);
    }

    public String getPlayerSuffix(UUID uuid) {
        return getFirstPlayerGroupValue(uuid, AEGroupSettings::getSuffix);
    }

    /**
     * Returns the group's own raw prefix, or null when unset - unlike {@link #handleGetGroupPrefix},
     * not formatted as a chat {@link Message}. Used to pre-fill the admin panel's settings form.
     *
     * @param groupName Group name.
     * @return Raw prefix value, or null when the group is unknown or has none set.
     */
    public String getGroupPrefix(String groupName) {
        return getPrefixValue(groupName);
    }

    /**
     * Returns the group's own raw suffix, or null when unset - see {@link #getGroupPrefix}.
     *
     * @param groupName Group name.
     * @return Raw suffix value, or null when the group is unknown or has none set.
     */
    public String getGroupSuffix(String groupName) {
        return getSuffixValue(groupName);
    }

    private String getPrefixValue(String groupName) {
        AEGroupSettings settings = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupSettings(groupName));
        return settings == null ? null : settings.getPrefix();
    }

    private String getSuffixValue(String groupName) {
        AEGroupSettings settings = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupSettings(groupName));
        return settings == null ? null : settings.getSuffix();
    }

    private void saveGroupSettings(String groupName, Consumer<AEGroupSettings> mutator) {
        AEDatabaseUtils.runUnchecked(() -> {
            AEGroupSettings settings = getStorage().getOrCreateGroupSettings(groupName);
            mutator.accept(settings);
            getStorage().saveGroupSettings(groupName, settings);
        });
    }

    private AEGroup getGroup(String groupName) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroup(groupName));
    }

    private String getFirstPlayerGroupValue(UUID uuid, java.util.function.Function<AEGroupSettings, String> accessor) {
        for (String groupName : getParentModule().getUsersModule().getGroupsForUser(uuid)) {
            AEGroupSettings settings = AEDatabaseUtils.supplyUnchecked(() -> getStorage().getGroupSettings(groupName));
            if (settings == null) {
                continue;
            }
            String value = accessor.apply(settings);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String normalizeLength(String value) {
        String normalized = value == null ? "" : value;
        int length = normalized.length();
        return length < NAME_MIN_LENGTH || length > NAME_MAX_LENGTH ? null : normalized;
    }

    private Message getSettingMessage(AEExecutionContext context, String langKey, String setting, String groupName, String value) {
        return getLangModule().buildMessage(langKey, context,
                args("setting", setting, "group", groupName, "value", value == null ? "null" : value));
    }

    private Message getMissingGroupMessage(AEExecutionContext context, String groupName) {
        return getLangModule().buildMessage("permissions.group.missing", context, args("group", groupName));
    }

    private Message getInvalidLengthMessage(AEExecutionContext context, String setting) {
        return getLangModule().buildMessage("permissions.group.setting.invalid", context,
                args("setting", setting, "min", String.valueOf(NAME_MIN_LENGTH), "max", String.valueOf(NAME_MAX_LENGTH)));
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
