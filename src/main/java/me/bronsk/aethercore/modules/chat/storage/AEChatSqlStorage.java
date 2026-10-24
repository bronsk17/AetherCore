package me.bronsk.aethercore.modules.chat.storage;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.database.storage.sql.AESqlStorage;
import me.bronsk.aethercore.modules.chat.models.AEChatGroupSettings;
import me.bronsk.aethercore.modules.chat.models.AEChatPlayerSettings;

import java.sql.SQLException;
import java.sql.Timestamp;

public class AEChatSqlStorage extends AESqlStorage implements AEChatStorage {

    public AEChatSqlStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws SQLException {
        super(connectionSource, dialect);
    }

    @Override
    protected void clearCache() {
    }

    @Override
    protected void createTables() throws SQLException {
        TableUtils.createTableIfNotExists(getConnection(), AEChatPlayerSettings.class);
        TableUtils.createTableIfNotExists(getConnection(), AEChatGroupSettings.class);

        ensureColumns(getPlayerSettingsDao(), "tcchatplayerssettings", AEChatPlayerSettings.PLAYER_UUID_FIELD);
        ensureColumns(getGroupSettingsDao(), "tcchatgroupssettings", AEChatGroupSettings.GROUP_NAME_FIELD);
    }

    @Override
    public AEChatPlayerSettings getChatPlayerSettings(String playerUuid) throws SQLException {
        return getPlayerSettingsDao().queryBuilder()
                .where()
                .eq(AEChatPlayerSettings.PLAYER_UUID_FIELD, playerUuid)
                .queryForFirst();
    }

    @Override
    public AEChatPlayerSettings getOrCreateChatPlayerSettings(String playerUuid) throws SQLException {
        AEChatPlayerSettings settings = getChatPlayerSettings(playerUuid);
        if (settings != null) {
            return settings;
        }

        Timestamp now = now();
        AEChatPlayerSettings created = new AEChatPlayerSettings(0, playerUuid, now, now);
        getPlayerSettingsDao().create(created);
        return created;
    }

    @Override
    public void saveChatPlayerSettings(AEChatPlayerSettings settings) throws SQLException {
        settings.setUpdatedAt(now());
        getPlayerSettingsDao().createOrUpdate(settings);
    }

    @Override
    public AEChatGroupSettings getChatGroupSettings(String groupName) throws SQLException {
        return getGroupSettingsDao().queryBuilder()
                .where()
                .eq(AEChatGroupSettings.GROUP_NAME_FIELD, groupName)
                .queryForFirst();
    }

    @Override
    public AEChatGroupSettings getOrCreateChatGroupSettings(String groupName) throws SQLException {
        AEChatGroupSettings settings = getChatGroupSettings(groupName);
        if (settings != null) {
            return settings;
        }

        Timestamp now = now();
        AEChatGroupSettings created = new AEChatGroupSettings(0, groupName, now, now);
        getGroupSettingsDao().create(created);
        return created;
    }

    @Override
    public void saveChatGroupSettings(AEChatGroupSettings settings) throws SQLException {
        settings.setUpdatedAt(now());
        getGroupSettingsDao().createOrUpdate(settings);
    }

    private void ensureColumns(Dao<?, ?> dao, String tableName, String targetColumnName) throws SQLException {
        ensureColumn(dao, tableName, targetColumnName, "VARCHAR(255)");
        ensureColumn(dao, tableName, "format", "VARCHAR(256)");
        ensureColumn(dao, tableName, "server_join_message", "VARCHAR(256)");
        ensureColumn(dao, tableName, "player_join_message", "VARCHAR(256)");
        ensureColumn(dao, tableName, "leave_message", "VARCHAR(256)");
        ensureColumn(dao, tableName, "first_join_message", "VARCHAR(256)");
        ensureColumn(dao, tableName, "broadcast_first_join_message", "BOOLEAN");
    }

    private Dao<AEChatPlayerSettings, Integer> getPlayerSettingsDao() throws SQLException {
        return getDao(AEChatPlayerSettings.class);
    }

    private Dao<AEChatGroupSettings, Integer> getGroupSettingsDao() throws SQLException {
        return getDao(AEChatGroupSettings.class);
    }
}
