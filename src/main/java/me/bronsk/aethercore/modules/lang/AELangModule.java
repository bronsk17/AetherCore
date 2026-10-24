package me.bronsk.aethercore.modules.lang;

import com.j256.ormlite.support.ConnectionSource;
import com.hypixel.hytale.server.core.Message;
import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.utils.AEMessageUtils;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.config.AEModuleConfig;
import me.bronsk.aethercore.modules.AEActionResult;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModule;
import me.bronsk.aethercore.modules.AEPageQuery;
import me.bronsk.aethercore.modules.AEPageResult;
import me.bronsk.aethercore.modules.lang.commands.AELangCommand;
import me.bronsk.aethercore.modules.lang.defaults.AEDefaultLanguage;
import me.bronsk.aethercore.modules.lang.defaults.AELangDefaultMessages;
import me.bronsk.aethercore.modules.lang.models.AELang;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;
import me.bronsk.aethercore.modules.lang.storage.AELangSqlStorage;
import me.bronsk.aethercore.modules.lang.storage.AELangStorage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Owns every translated message. Messages are stored in the database (so an admin can edit them
 * with {@code /aether lang set} at runtime) but resolution never hits the database - {@link
 * #reloadMessages()} snapshots every language into memory once (at boot, right after default
 * messages are installed, and again after any {@code /aether lang} mutation), and every {@code
 * buildText}/{@code buildMessage} call reads that snapshot.
 */
public class AELangModule extends AEModule<AEModuleConfig, AELangStorage> {

    public static final String NAME = "lang";

    private volatile Map<String, Map<String, String>> messagesByLang = Map.of();

    @Override
    protected void onInit() {
        getModulesManager().getDatabaseInstaller().registerInstaller(new AEDefaultLanguage(this));
        getModulesManager().getDatabaseInstaller().registerInstaller(new AELangDefaultMessages(this));
        AetherPlugin.getPlugin().getRootCommand().addSubCommand(new AELangCommand());
    }

    @Override
    protected AELangStorage createStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws Exception {
        return new AELangSqlStorage(connectionSource, dialect);
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Rebuilds the in-memory snapshot every resolution reads from. Call once after the database
     * installers have run (default messages must exist first), and again after any {@code
     * /aether lang} command mutates a language or a message, so the change is live without a restart.
     *
     * @throws SQLException when languages or messages cannot be read.
     */
    public void reloadMessages() throws SQLException {
        Map<String, Map<String, String>> snapshot = new HashMap<>();
        for (AELang lang : getStorage().getLangs()) {
            snapshot.put(lang.getCode(), Map.copyOf(getStorage().getTranslationsForLang(lang.getCode())));
        }
        messagesByLang = Map.copyOf(snapshot);
    }

    public Message buildMessage(String code, Map<String, String> args) {
        return buildMessageForLang(getPluginLang(), code, args);
    }

    public Message buildMessage(String code, AEExecutionContext context, Map<String, String> args) {
        return buildMessageForLang(resolveContextLang(context), code, args);
    }

    public Message buildMessage(String code) {
        return buildMessage(code, Map.of());
    }

    public Message buildMessage(String code, AEExecutionContext context) {
        return buildMessage(code, context, Map.of());
    }

    public Message buildMessageForLang(String langCode, String code, Map<String, String> args) {
        return AEMessageUtils.fromColoredString(buildTextForLang(langCode, code, args));
    }

    public String buildText(String code, Map<String, String> args) {
        return buildTextForLang(getPluginLang(), code, args);
    }

    public String buildText(String code, AEExecutionContext context, Map<String, String> args) {
        return buildTextForLang(resolveContextLang(context), code, args);
    }

    public String buildText(String code) {
        return buildText(code, Map.of());
    }

    public String buildText(String code, AEExecutionContext context) {
        return buildText(code, context, Map.of());
    }

    public String buildTextForLang(String langCode, String code, Map<String, String> args) {
        return applyArgs(getMessageText(langCode, code), args);
    }

    public Message handleAddLang(AEExecutionContext context, String langCode, String description) {
        String normalizedLangCode = normalizeRequired(langCode);
        String normalizedDescription = normalizeRequired(description);
        if (normalizedLangCode == null || normalizedDescription == null) {
            return buildMessage("lang.validation.required", context);
        }
        if (resolveLangCode(normalizedLangCode) != null) {
            return buildMessage("lang.add.exists", context, args("lang", normalizedLangCode));
        }

        AELang created = AEDatabaseUtils.supplyUnchecked(
                () -> getStorage().addLang(normalizedLangCode, normalizedDescription));
        if (created == null) {
            return buildMessage("lang.add.exists", context, args("lang", normalizedLangCode));
        }

        reloadMessagesUnchecked();
        return buildMessage("lang.add.success", context, args("lang", normalizedLangCode, "description", normalizedDescription));
    }

    public Message handleRemoveLang(AEExecutionContext context, String langCode) {
        String normalizedLangCode = normalizeRequired(langCode);
        if (normalizedLangCode == null) {
            return buildMessage("lang.validation.required", context);
        }
        if (getPluginLang().equalsIgnoreCase(normalizedLangCode)) {
            return buildMessage("lang.remove.default", context, args("lang", normalizedLangCode));
        }

        AELang removed = AEDatabaseUtils.supplyUnchecked(() -> getStorage().removeLang(normalizedLangCode));
        if (removed == null) {
            return buildMessage("lang.missing", context, args("lang", normalizedLangCode));
        }

        reloadMessagesUnchecked();
        return buildMessage("lang.remove.success", context, args("lang", removed.getCode()));
    }

    /** Sets one language's translation for {@code code}, creating the message (no description) when it's new. */
    public Message handleSetLangMessage(AEExecutionContext context, String langCode, String code, String messageText) {
        String normalizedLangCode = normalizeRequired(langCode);
        String normalizedCode = normalizeRequired(code);
        String normalizedMessageText = normalizeRequired(messageText);
        if (normalizedLangCode == null || normalizedCode == null || normalizedMessageText == null) {
            return buildMessage("lang.validation.required", context);
        }

        String resolvedLangCode = resolveLangCode(normalizedLangCode);
        if (resolvedLangCode == null) {
            return buildMessage("lang.missing", context, args("lang", normalizedLangCode));
        }

        AEDatabaseUtils.runUnchecked(() -> {
            if (getStorage().getMessage(normalizedCode) == null) {
                getStorage().createMessage(normalizedCode, null);
            }
            getStorage().setTranslation(resolvedLangCode, normalizedCode, normalizedMessageText);
        });
        reloadMessagesUnchecked();
        return buildMessage("lang.set.success", context, args("lang", resolvedLangCode, "code", normalizedCode));
    }

    /** Creates a new message - fails without touching anything if the code is already taken. Used by the Lang tab's "New Message" Save. */
    public AEActionResult handleCreateLangMessage(AEExecutionContext context, String code, String description, Map<String, String> translations) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }
        if (getMessage(normalizedCode) != null) {
            return AEActionResult.error(buildMessage("lang.message.exists", context, args("code", normalizedCode)));
        }

        AEDatabaseUtils.runUnchecked(() -> {
            getStorage().createMessage(normalizedCode, description);
            applyTranslations(normalizedCode, translations);
        });
        reloadMessagesUnchecked();
        return AEActionResult.success(buildMessage("lang.message.create.success", context, args("code", normalizedCode)));
    }

    /** Updates an existing message's description and translations - used by the Lang tab's edit-popup Save. */
    public AEActionResult handleUpdateLangMessage(AEExecutionContext context, String code, String description, Map<String, String> translations) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }
        if (getMessage(normalizedCode) == null) {
            return AEActionResult.error(buildMessage("lang.message-code.missing", context, args("code", normalizedCode)));
        }

        AEDatabaseUtils.runUnchecked(() -> {
            getStorage().setMessageDescription(normalizedCode, description);
            applyTranslations(normalizedCode, translations);
        });
        reloadMessagesUnchecked();
        return AEActionResult.success(buildMessage("lang.message.update.success", context, args("code", normalizedCode)));
    }

    private void applyTranslations(String code, Map<String, String> translations) throws SQLException {
        for (Map.Entry<String, String> translation : translations.entrySet()) {
            String text = translation.getValue();
            if (text != null && !text.isBlank()) {
                getStorage().setTranslation(translation.getKey(), code, text);
            }
        }
    }

    public AEActionResult handleDeleteLangMessage(AEExecutionContext context, String code) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }

        AELangMessage removed = AEDatabaseUtils.supplyUnchecked(() -> getStorage().deleteMessage(normalizedCode));
        if (removed == null) {
            return AEActionResult.error(buildMessage("lang.message-code.missing", context, args("code", normalizedCode)));
        }

        reloadMessagesUnchecked();
        return AEActionResult.success(buildMessage("lang.message.delete.success", context, args("code", removed.getCode())));
    }

    /** Creates a new language - fails without creating anything if the code is already taken. Used by the Lang tab's Langs sub-tab. */
    public AEActionResult handleCreateLang(AEExecutionContext context, String code, String description) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }
        if (resolveLangCode(normalizedCode) != null) {
            return AEActionResult.error(buildMessage("lang.add.exists", context, args("lang", normalizedCode)));
        }

        AELang created = AEDatabaseUtils.supplyUnchecked(() -> getStorage().addLang(normalizedCode, description));
        if (created == null) {
            return AEActionResult.error(buildMessage("lang.add.exists", context, args("lang", normalizedCode)));
        }

        reloadMessagesUnchecked();
        return AEActionResult.success(buildMessage("lang.add.success", context, args("lang", normalizedCode, "description", formatDescription(description))));
    }

    /** Updates an existing language's description - used by the Lang tab's Langs sub-tab edit Save. */
    public AEActionResult handleUpdateLang(AEExecutionContext context, String code, String description) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }
        String resolvedCode = resolveLangCode(normalizedCode);
        if (resolvedCode == null) {
            return AEActionResult.error(buildMessage("lang.missing", context, args("lang", normalizedCode)));
        }

        AEDatabaseUtils.runUnchecked(() -> getStorage().setLangDescription(resolvedCode, description));
        return AEActionResult.success(buildMessage("lang.desc.success", context, args("lang", resolvedCode, "description", formatDescription(description))));
    }

    /** Deletes a language and every translation of it - refuses to remove the plugin's own default language. Used by the Lang tab's Langs sub-tab Delete. */
    public AEActionResult handleDeleteLang(AEExecutionContext context, String code) {
        String normalizedCode = normalizeRequired(code);
        if (normalizedCode == null) {
            return AEActionResult.error(buildMessage("lang.validation.required", context));
        }
        if (getPluginLang().equalsIgnoreCase(normalizedCode)) {
            return AEActionResult.error(buildMessage("lang.remove.default", context, args("lang", normalizedCode)));
        }

        AELang removed = AEDatabaseUtils.supplyUnchecked(() -> getStorage().removeLang(normalizedCode));
        if (removed == null) {
            return AEActionResult.error(buildMessage("lang.missing", context, args("lang", normalizedCode)));
        }

        reloadMessagesUnchecked();
        return AEActionResult.success(buildMessage("lang.remove.success", context, args("lang", removed.getCode())));
    }

    /** One page of registered languages for the Lang tab's Langs sub-tab table - the list is admin-panel-scale, no need for a DB-level query. */
    public AEPageResult<AELang> getLangQueryResult(AEPageQuery query) {
        List<AELang> all = AEDatabaseUtils.supplyUnchecked(getStorage()::getLangs);
        List<AELang> filtered = query.hasSearch()
                ? all.stream().filter(lang -> matchesLangSearch(lang, query.search())).toList()
                : all;
        int fromIndex = Math.min((int) query.offset(), filtered.size());
        int toIndex = Math.min(fromIndex + query.pageSize(), filtered.size());
        return new AEPageResult<>(query, filtered.subList(fromIndex, toIndex), filtered.size());
    }

    public AELang getLang(String code) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getLang(code));
    }

    private static boolean matchesLangSearch(AELang lang, String search) {
        String needle = search.toLowerCase(Locale.ROOT);
        return lang.getCode().toLowerCase(Locale.ROOT).contains(needle)
                || (lang.getDescription() != null && lang.getDescription().toLowerCase(Locale.ROOT).contains(needle));
    }

    public Message handleSetLangDescription(AEExecutionContext context, String langCode, String description) {
        String normalizedLangCode = normalizeRequired(langCode);
        String normalizedDescription = normalizeRequired(description);
        if (normalizedLangCode == null || normalizedDescription == null) {
            return buildMessage("lang.validation.required", context);
        }

        String resolvedLangCode = resolveLangCode(normalizedLangCode);
        if (resolvedLangCode == null) {
            return buildMessage("lang.missing", context, args("lang", normalizedLangCode));
        }

        AEDatabaseUtils.runUnchecked(() -> getStorage().setLangDescription(resolvedLangCode, normalizedDescription));
        return buildMessage("lang.desc.success", context, args("lang", resolvedLangCode, "description", normalizedDescription));
    }

    /** One page of message codes for the Lang tab's table. */
    public AEPageResult<AELangMessage> getMessageQueryResult(AEPageQuery query) {
        return AEDatabaseUtils.supplyUnchecked(() -> new AEPageResult<>(
                query,
                getStorage().getMessages(query.search(), query.offset(), query.pageSize()),
                getStorage().countMessages(query.search())));
    }

    public AELangMessage getMessage(String code) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getMessage(code));
    }

    /** Every translation for one message, keyed by language code - used to pre-fill the edit popup. */
    public Map<String, String> getTranslations(String code) {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getTranslations(code));
    }

    /** Every message code's translation in {@code langCode}, from the in-memory snapshot ({@link #reloadMessages()}) - used to show the plugin-language translation next to each message code. */
    public Map<String, String> getMessagesForLang(String langCode) {
        return messagesByLang.getOrDefault(langCode, Map.of());
    }

    public Message handleListLangs(AEExecutionContext context) {
        List<AELang> langs = AEDatabaseUtils.supplyUnchecked(getStorage()::getLangs);
        Message response = Message.empty();
        response.insert(buildMessage("lang.list.title", context));
        for (AELang lang : langs) {
            response.insert("\n");
            response.insert(AEMessageUtils.fromColoredString(
                    "&6" + lang.getCode() + "&7 - &f" + formatDescription(lang.getDescription())));
        }
        return response;
    }

    public List<String> getLangCodes() {
        return AEDatabaseUtils.supplyUnchecked(getStorage()::getLangCodes);
    }

    public List<String> getMessageCodes() {
        return AEDatabaseUtils.supplyUnchecked(getStorage()::getMessageCodes);
    }

    public void installDefaultLang(String langCode, String description) throws SQLException {
        getStorage().getOrCreateLang(langCode, description);
    }

    /** Installs a default translation without overwriting an admin edit - creates the message (no description) when new. */
    public void installDefaultMessage(String langCode, String code, String message) throws SQLException {
        if (getStorage().getMessage(code) == null) {
            getStorage().createMessage(code, null);
        }
        if (!getStorage().getTranslations(code).containsKey(langCode)) {
            getStorage().setTranslation(langCode, code, message);
        }
    }

    /** Case-insensitive match against known language codes - returns the stored (canonical-case) code, or null. */
    public String resolveLangCode(String langCode) {
        for (String registeredLangCode : getLangCodes()) {
            if (registeredLangCode.equalsIgnoreCase(langCode)) {
                return registeredLangCode;
            }
        }
        return null;
    }

    /** Case-insensitive match against known message codes - returns the stored (canonical-case) code, or null. */
    public String resolveMessageCode(String messageCode) {
        for (String registeredMessageCode : getMessageCodes()) {
            if (registeredMessageCode.equalsIgnoreCase(messageCode)) {
                return registeredMessageCode;
            }
        }
        return null;
    }

    public static Map<String, String> args(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Message arguments must be key/value pairs");
        }

        Map<String, String> args = new HashMap<>();
        for (int index = 0; index < pairs.length; index += 2) {
            args.put(pairs[index], pairs[index + 1]);
        }
        return args;
    }

    private String getMessageText(String langCode, String code) {
        Map<String, String> langMessages = messagesByLang.get(langCode);
        if (langMessages != null) {
            String message = langMessages.get(code);
            if (message != null) {
                return message;
            }
        }

        String pluginLang = getPluginLang();
        if (!pluginLang.equals(langCode)) {
            Map<String, String> fallbackMessages = messagesByLang.get(pluginLang);
            if (fallbackMessages != null) {
                String fallbackMessage = fallbackMessages.get(code);
                if (fallbackMessage != null) {
                    return fallbackMessage;
                }
            }
        }

        return code;
    }

    private String getPluginLang() {
        return getModulesManager().getGlobalConfig().getPluginLang();
    }

    private String resolveContextLang(AEExecutionContext context) {
        if (context == null || context.lang() == null || context.lang().isBlank()) {
            return getPluginLang();
        }
        return context.lang();
    }

    private void reloadMessagesUnchecked() {
        AEDatabaseUtils.runUnchecked(this::reloadMessages);
    }

    private String applyArgs(String text, Map<String, String> args) {
        if (text == null || args == null || args.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : args.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", escapeFormatting(entry.getValue()));
        }
        return result;
    }

    private String escapeFormatting(String value) {
        return value == null ? "" : value.replace("&", "&&");
    }

    private String normalizeRequired(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized;
    }

    private String formatDescription(String description) {
        if (description == null || description.isBlank()) {
            return "-";
        }
        return description;
    }
}
