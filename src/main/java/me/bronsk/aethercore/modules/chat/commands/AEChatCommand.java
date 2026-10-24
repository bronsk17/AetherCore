package me.bronsk.aethercore.modules.chat.commands;

import me.bronsk.aethercore.commands.AECollectionCommand;

public class AEChatCommand extends AECollectionCommand {

    public AEChatCommand() {
        super("chat", "AetherCore chat command");
        addSubCommand(new AEChatGroupCommand());
        addSubCommand(new AEChatPlayerCommand());
    }
}
