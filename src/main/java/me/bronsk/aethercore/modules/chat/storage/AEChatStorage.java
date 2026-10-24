package me.bronsk.aethercore.modules.chat.storage;

import me.bronsk.aethercore.modules.chat.models.AEChatGroupSettings;
import me.bronsk.aethercore.modules.chat.models.AEChatPlayerSettings;

import java.sql.SQLException;

public interface AEChatStorage extends AutoCloseable {

    AEChatPlayerSettings getChatPlayerSettings(String playerUuid) throws SQLException;

    AEChatPlayerSettings getOrCreateChatPlayerSettings(String playerUuid) throws SQLException;

    void saveChatPlayerSettings(AEChatPlayerSettings settings) throws SQLException;

    AEChatGroupSettings getChatGroupSettings(String groupName) throws SQLException;

    AEChatGroupSettings getOrCreateChatGroupSettings(String groupName) throws SQLException;

    void saveChatGroupSettings(AEChatGroupSettings settings) throws SQLException;

    @Override
    void close();
}
