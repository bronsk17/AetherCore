package me.bronsk.aethercore.modules.lang.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.sql.AESqlStorage;
import me.bronsk.aethercore.modules.lang.models.AELang;
import me.bronsk.aethercore.modules.lang.models.AELangMessage;
import me.bronsk.aethercore.modules.lang.models.AELangMessageTranslation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AELangSqlStorage extends AESqlStorage implements AELangStorage {

    public AELangSqlStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws SQLException {
        super(connectionSource, dialect);
    }

    @Override
    public void createTables() throws SQLException {
        ConnectionSource connectionSource = getConnection();
        TableUtils.createTableIfNotExists(connectionSource, AELang.class);
        TableUtils.createTableIfNotExists(connectionSource, AELangMessage.class);
        TableUtils.createTableIfNotExists(connectionSource, AELangMessageTranslation.class);
        ensureColumn(getLangsDao(), "tclangs", AELang.DESCRIPTION_FIELD, "VARCHAR(255)");
        ensureRestrictForeignKey(getTranslationsDao(), "tclangmessagetranslations", "fk_tclangmessagetranslations_lang_id",
                AELangMessageTranslation.LANG_ID_FIELD, "tclangs", AELang.ID_FIELD);
        ensureRestrictForeignKey(getTranslationsDao(), "tclangmessagetranslations", "fk_tclangmessagetranslations_message_id",
                AELangMessageTranslation.MESSAGE_ID_FIELD, "tclangmessages", AELangMessage.ID_FIELD);
    }

    @Override
    public AELang getLang(String code) throws SQLException {
        return getLangsDao().queryBuilder()
                .where()
                .eq(AELang.CODE_FIELD, code)
                .queryForFirst();
    }

    @Override
    public AELang getOrCreateLang(String code) throws SQLException {
        return getOrCreateLang(code, code);
    }

    @Override
    public AELang getOrCreateLang(String code, String description) throws SQLException {
        AELang lang = getLang(code);
        if (lang != null) {
            if ((lang.getDescription() == null || lang.getDescription().isBlank())
                    && description != null && !description.isBlank()) {
                lang.setDescription(description);
                lang.setUpdatedAt(now());
                getLangsDao().update(lang);
            }
            return lang;
        }

        AELang created = new AELang(0, code, description, now(), now());
        getLangsDao().create(created);
        return created;
    }

    @Override
    public AELang addLang(String code, String description) throws SQLException {
        if (getLang(code) != null) {
            return null;
        }

        AELang created = new AELang(0, code, description, now(), now());
        getLangsDao().create(created);
        return created;
    }

    @Override
    public AELang removeLang(String code) throws SQLException {
        AELang lang = getLang(code);
        if (lang == null) {
            return null;
        }

        getTranslationsDao().delete(getTranslationsDao().queryBuilder()
                .where()
                .eq(AELangMessageTranslation.LANG_ID_FIELD, lang.getId())
                .query());
        getLangsDao().delete(lang);
        return lang;
    }

    @Override
    public AELang setLangDescription(String code, String description) throws SQLException {
        AELang lang = getLang(code);
        if (lang == null) {
            return null;
        }

        lang.setDescription(description);
        lang.setUpdatedAt(now());
        getLangsDao().update(lang);
        return lang;
    }

    @Override
    public List<AELang> getLangs() throws SQLException {
        return getLangsDao().queryBuilder()
                .orderBy(AELang.CODE_FIELD, true)
                .query();
    }

    @Override
    public List<String> getLangCodes() throws SQLException {
        List<String> codes = new ArrayList<>();
        for (AELang lang : getLangs()) {
            codes.add(lang.getCode());
        }
        return codes;
    }

    @Override
    public AELangMessage getMessage(String code) throws SQLException {
        return getMessagesDao().queryBuilder()
                .where()
                .eq(AELangMessage.CODE_FIELD, code)
                .queryForFirst();
    }

    @Override
    public List<AELangMessage> getMessages(String search, long offset, int limit) throws SQLException {
        QueryBuilder<AELangMessage, Integer> builder = getMessagesDao().queryBuilder();
        applySearch(builder, search);
        return builder
                .orderBy(AELangMessage.CODE_FIELD, true)
                .offset(offset)
                .limit((long) limit)
                .query();
    }

    @Override
    public long countMessages(String search) throws SQLException {
        QueryBuilder<AELangMessage, Integer> builder = getMessagesDao().queryBuilder();
        applySearch(builder, search);
        return builder.countOf();
    }

    @Override
    public List<String> getMessageCodes() throws SQLException {
        List<String> codes = new ArrayList<>();
        for (AELangMessage message : getMessagesDao().queryForAll()) {
            codes.add(message.getCode());
        }
        return codes;
    }

    @Override
    public AELangMessage createMessage(String code, String description) throws SQLException {
        if (getMessage(code) != null) {
            return null;
        }

        AELangMessage created = new AELangMessage(0, code, description, now(), now());
        getMessagesDao().create(created);
        return created;
    }

    @Override
    public AELangMessage setMessageDescription(String code, String description) throws SQLException {
        AELangMessage message = getMessage(code);
        if (message == null) {
            return null;
        }

        message.setDescription(description);
        message.setUpdatedAt(now());
        getMessagesDao().update(message);
        return message;
    }

    @Override
    public AELangMessage deleteMessage(String code) throws SQLException {
        AELangMessage message = getMessage(code);
        if (message == null) {
            return null;
        }

        getTranslationsDao().delete(getTranslationsDao().queryBuilder()
                .where()
                .eq(AELangMessageTranslation.MESSAGE_ID_FIELD, message.getId())
                .query());
        getMessagesDao().delete(message);
        return message;
    }

    @Override
    public Map<String, String> getTranslations(String code) throws SQLException {
        AELangMessage message = getMessage(code);
        if (message == null) {
            return Map.of();
        }

        Map<Integer, String> langCodesById = getLangCodesById();
        List<AELangMessageTranslation> translations = getTranslationsDao().queryBuilder()
                .where()
                .eq(AELangMessageTranslation.MESSAGE_ID_FIELD, message.getId())
                .query();

        Map<String, String> result = new HashMap<>();
        for (AELangMessageTranslation translation : translations) {
            String langCode = langCodesById.get(translation.getLangId());
            if (langCode != null) {
                result.put(langCode, translation.getText());
            }
        }
        return result;
    }

    @Override
    public Map<String, String> getTranslationsForLang(String langCode) throws SQLException {
        AELang lang = getLang(langCode);
        if (lang == null) {
            return Map.of();
        }

        Map<Integer, String> messageCodesById = getMessageCodesById();
        List<AELangMessageTranslation> translations = getTranslationsDao().queryBuilder()
                .where()
                .eq(AELangMessageTranslation.LANG_ID_FIELD, lang.getId())
                .query();

        Map<String, String> result = new HashMap<>();
        for (AELangMessageTranslation translation : translations) {
            String code = messageCodesById.get(translation.getMessageId());
            if (code != null) {
                result.put(code, translation.getText());
            }
        }
        return result;
    }

    @Override
    public void setTranslation(String langCode, String code, String text) throws SQLException {
        AELang lang = getLang(langCode);
        if (lang == null) {
            throw new SQLException("Unknown language: " + langCode);
        }
        AELangMessage message = getMessage(code);
        if (message == null) {
            throw new SQLException("Unknown message code: " + code);
        }

        AELangMessageTranslation existing = getTranslationsDao().queryBuilder()
                .where()
                .eq(AELangMessageTranslation.LANG_ID_FIELD, lang.getId())
                .and()
                .eq(AELangMessageTranslation.MESSAGE_ID_FIELD, message.getId())
                .queryForFirst();

        if (existing == null) {
            AELangMessageTranslation created = new AELangMessageTranslation(0, lang.getId(), message.getId(), text, now(), now());
            getTranslationsDao().create(created);
            return;
        }

        existing.setText(text);
        existing.setUpdatedAt(now());
        getTranslationsDao().update(existing);
    }

    private void applySearch(QueryBuilder<AELangMessage, Integer> builder, String search) throws SQLException {
        if (search == null || search.isBlank()) {
            return;
        }
        Where<AELangMessage, Integer> where = builder.where();
        where.like(AELangMessage.CODE_FIELD, "%" + search + "%")
                .or()
                .like("description", "%" + search + "%");
    }

    private Map<Integer, String> getLangCodesById() throws SQLException {
        Map<Integer, String> result = new HashMap<>();
        for (AELang lang : getLangs()) {
            result.put(lang.getId(), lang.getCode());
        }
        return result;
    }

    private Map<Integer, String> getMessageCodesById() throws SQLException {
        Map<Integer, String> result = new HashMap<>();
        for (AELangMessage message : getMessagesDao().queryForAll()) {
            result.put(message.getId(), message.getCode());
        }
        return result;
    }

    private Dao<AELang, Integer> getLangsDao() throws SQLException {
        return getDao(AELang.class);
    }

    private Dao<AELangMessage, Integer> getMessagesDao() throws SQLException {
        return getDao(AELangMessage.class);
    }

    private Dao<AELangMessageTranslation, Integer> getTranslationsDao() throws SQLException {
        return getDao(AELangMessageTranslation.class);
    }

    @Override
    protected void clearCache() {
    }
}
