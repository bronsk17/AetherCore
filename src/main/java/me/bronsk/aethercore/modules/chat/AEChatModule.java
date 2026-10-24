package me.bronsk.aethercore.modules.chat;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.modules.AEModule;
import me.bronsk.aethercore.modules.chat.commands.AEChatCommand;
import me.bronsk.aethercore.modules.chat.models.AEChatSettings;
import me.bronsk.aethercore.modules.chat.storage.AEChatSqlStorage;
import me.bronsk.aethercore.modules.chat.storage.AEChatStorage;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.player.AEGamePlayer;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.text.AETextContext;
import me.bronsk.aethercore.text.AETextPlaceholders;
import me.bronsk.aethercore.utils.AEMessageUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AEChatModule extends AEModule<AEChatConfig, AEChatStorage> {

    public static final String NAME = "chat";

    public AEChatModule() {
        addSubModule(new AEChatSettingsModule());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void onInit() {
        registerChatPlaceholderResolvers();
        registerListener(new AEChatListener(this));
        AetherPlugin.getPlugin().getRootCommand().addSubCommand(new AEChatCommand());
    }

    @Override
    protected List<Class<? extends AEModule<?, ?>>> getRequiredModules() {
        return List.of(AEPlayersModule.class, AEPermissionsModule.class);
    }

    @Override
    protected AEChatStorage createStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws Exception {
        return new AEChatSqlStorage(connectionSource, dialect);
    }

    public AEChatSettingsModule getSettingsModule() {
        return getSubModule(AEChatSettingsModule.class);
    }

    public Message getChatFormat(AETextContext context) {
        try {
            AEGamePlayer gamePlayer = context.player();
            Set<String> groupNames = getSettingsModule().getGroupsForUser(gamePlayer.getUuid());
            String format = getSettingsModule().getSettingForPlayerOrGroups(gamePlayer.getUuid(), AEChatSettings::getFormat, getConfig().getDefaultFormat(), groupNames);
            String inputString = AETextPlaceholders.get().resolve(format, context);
            return AEMessageUtils.fromColoredString(inputString.trim());
        } catch (Exception exception) {
            AELogger.logException("Failed to resolve chat format, falling back to raw message", exception);
            return Message.raw(context.player().getUsername() + ": " + context.rawMessage());
        }
    }

    public Message getServerJoinMessage(AETextContext context) {
        AEGamePlayer gamePlayer = context.player();
        String code = getSettingsModule().getSettingForPlayerOrGroups(gamePlayer.getUuid(), AEChatSettings::getServerJoinMessageCode,
                getConfig().getDefaultServerJoinMessageCode(), getSettingsModule().getGroupsForUser(gamePlayer.getUuid()));
        return getTemplateMessage(context, code);
    }

    public Message getPlayerJoinMessage(AETextContext context) {
        AEGamePlayer gamePlayer = context.player();
        String code = getSettingsModule().getSettingForPlayerOrGroups(gamePlayer.getUuid(), AEChatSettings::getPlayerJoinMessageCode,
                getConfig().getDefaultPlayerJoinMessageCode(), getSettingsModule().getGroupsForUser(gamePlayer.getUuid()));
        return getTemplateMessage(context, code);
    }

    public Message getFirstJoinMessage(AETextContext context) {
        AEGamePlayer gamePlayer = context.player();
        String code = getSettingsModule().getSettingForPlayerOrGroups(gamePlayer.getUuid(), AEChatSettings::getFirstJoinMessageCode,
                getConfig().getDefaultFirstJoinMessageCode(), getSettingsModule().getGroupsForUser(gamePlayer.getUuid()));
        return getTemplateMessage(context, code);
    }

    public boolean shouldBroadcastFirstJoinMessage(AETextContext context) {
        AEGamePlayer gamePlayer = context.player();
        Boolean configDefault = getConfig().getDefaultBroadcastFirstJoinMessage();
        String defaultValue = configDefault == null ? null : String.valueOf(configDefault);
        String value = getSettingsModule().getSettingForPlayerOrGroups(gamePlayer.getUuid(), settings -> settings.getBroadcastFirstJoinMessage() == null ? null : String.valueOf(settings.getBroadcastFirstJoinMessage()),
                defaultValue, getSettingsModule().getGroupsForUser(gamePlayer.getUuid()));
        return Boolean.TRUE.equals(value == null ? null : Boolean.valueOf(value));
    }

    /** 
     * Builds the leave broadcast directly from a {@link PlayerRef} - at disconnect there is no
     * entity left, so a {@link AEGamePlayer}/{@link AETextContext} can't be built for this one.
     */
    public Message getLeaveMessage(PlayerRef playerRef) {
        UUID uuid = playerRef.getUuid();
        String code = getSettingsModule().getSettingForPlayerOrGroups(uuid, AEChatSettings::getLeaveMessageCode,
                getConfig().getDefaultLeaveMessageCode(), getSettingsModule().getGroupsForUser(uuid));
        if (code == null || code.isEmpty()) {
            return null;
        }

        String template = getLangModule().buildTextForLang(resolveLang(playerRef), code, Map.of());
        String resolved = template.replace("{player}", playerRef.getUsername()).replace("{uuid}", uuid.toString());
        return AEMessageUtils.fromColoredString(resolved);
    }

    private Message getTemplateMessage(AETextContext context, String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        String template = getLangModule().buildTextForLang(resolveLang(context.player().getPlayerRef()), code, Map.of());
        return AEMessageUtils.fromColoredString(AETextPlaceholders.get().resolve(template, context.withRawMessage(template)));
    }

    private String resolveLang(PlayerRef playerRef) {
        String lang = playerRef == null ? null : playerRef.getLanguage();
        return lang == null || lang.isBlank() ? getModulesManager().getGlobalConfig().getPluginLang() : lang;
    }

    private AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }

    private void registerChatPlaceholderResolvers() {
        AETextPlaceholders.get().register("message", context -> context.player() != null
                && context.player().hasPermission(AEChatPerms.CHAT_COLORIZE)
                ? context.rawMessage()
                : AEMessageUtils.escapeFormatting(context.rawMessage()));
    }
}
