package me.bronsk.aethercore.modules.lang.storage;

import me.bronsk.aethercore.modules.lang.models.AELang;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface AELangStorage extends AutoCloseable {

    AELang getLang(String code) throws SQLException;

    AELang getOrCreateLang(String code) throws SQLException;

    AELang getOrCreateLang(String code, String description) throws SQLException;

    AELang addLang(String code, String description) throws SQLException;

    AELang removeLang(String code) throws SQLException;

    /** Returns null when the language doesn't exist. */
    AELang setLangDescription(String code, String description) throws SQLException;

    List<AELang> getLangs() throws SQLException;

    List<String> getLangCodes() throws SQLException;

    AELangMessage getMessage(String code) throws SQLException;

    /** One page of messages, ordered by code, matching {@code search} against code or description. */
    List<AELangMessage> getMessages(String search, long offset, int limit) throws SQLException;

    long countMessages(String search) throws SQLException;

    List<String> getMessageCodes() throws SQLException;

    /** Returns null when {@code code} already exists. */
    AELangMessage createMessage(String code, String description) throws SQLException;

    /** Returns null when the message doesn't exist. */
    AELangMessage setMessageDescription(String code, String description) throws SQLException;

    /** Removes the message and every translation of it. Returns null when it didn't exist. */
    AELangMessage deleteMessage(String code) throws SQLException;

    /** Every translation for one message, keyed by language code. */
    Map<String, String> getTranslations(String code) throws SQLException;

    /** Every translation for one language, keyed by message code - used to preload the in-memory snapshot. */
    Map<String, String> getTranslationsForLang(String langCode) throws SQLException;

    /** Requires both the language and the message to already exist. */
    void setTranslation(String langCode, String code, String text) throws SQLException;

    @Override
    void close();
}
