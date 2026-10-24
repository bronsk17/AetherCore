package me.bronsk.aethercore.modules.permissions.commands;

import com.hypixel.hytale.server.core.permissions.commands.PermReloadCommand;
import com.hypixel.hytale.server.core.permissions.commands.PermTestCommand;
import me.bronsk.aethercore.commands.AECollectionCommand;

public class AEPermCommand extends AECollectionCommand {

    /**
     * Creates a new instance.
     */
    public AEPermCommand() {
        super("permissions", "AetherCore permissions");
        addSubCommand(new AEPermGroupCommand());
        addSubCommand(new AEPermUserCommand());
        addSubCommand(new AEPermListCommand());
        addSubCommand(new PermTestCommand());
        addSubCommand(new PermReloadCommand());
    }

}
