package me.bronsk.aethercore.modules.chat;

import me.bronsk.aethercore.database.storage.config.AEModuleConfig;

public class AEChatConfig extends AEModuleConfig {

    private String defaultFormat = "{prefix} {player} {suffix}: {message}";

    private String defaultServerJoinMessageCode = "chat.broadcast.server-join";

    private String defaultPlayerJoinMessageCode;

    private String defaultLeaveMessageCode = "chat.broadcast.leave";

    private String defaultFirstJoinMessageCode = "chat.broadcast.first-join";

    private Boolean defaultBroadcastFirstJoinMessage = true;

    public AEChatConfig() {
    }

    public String getDefaultFormat() {
        return defaultFormat;
    }

    public void setDefaultFormat(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public String getDefaultServerJoinMessageCode() {
        return defaultServerJoinMessageCode;
    }

    public void setDefaultServerJoinMessageCode(String defaultServerJoinMessageCode) {
        this.defaultServerJoinMessageCode = defaultServerJoinMessageCode;
    }

    public String getDefaultPlayerJoinMessageCode() {
        return defaultPlayerJoinMessageCode;
    }

    public void setDefaultPlayerJoinMessageCode(String defaultPlayerJoinMessageCode) {
        this.defaultPlayerJoinMessageCode = defaultPlayerJoinMessageCode;
    }

    public String getDefaultLeaveMessageCode() {
        return defaultLeaveMessageCode;
    }

    public void setDefaultLeaveMessageCode(String defaultLeaveMessageCode) {
        this.defaultLeaveMessageCode = defaultLeaveMessageCode;
    }

    public String getDefaultFirstJoinMessageCode() {
        return defaultFirstJoinMessageCode;
    }

    public void setDefaultFirstJoinMessageCode(String defaultFirstJoinMessageCode) {
        this.defaultFirstJoinMessageCode = defaultFirstJoinMessageCode;
    }

    public Boolean getDefaultBroadcastFirstJoinMessage() {
        return defaultBroadcastFirstJoinMessage;
    }

    public void setDefaultBroadcastFirstJoinMessage(Boolean defaultBroadcastFirstJoinMessage) {
        this.defaultBroadcastFirstJoinMessage = defaultBroadcastFirstJoinMessage;
    }
}
