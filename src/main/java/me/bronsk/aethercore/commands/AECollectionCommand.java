package me.bronsk.aethercore.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;

public abstract class AECollectionCommand extends AECommand {

    /**
     * Creates a command namespace that mainly groups subcommands.
     *
     * @param name Command name.
     * @param description Command description.
     */
    protected AECollectionCommand(String name, String description) {
        super(name, description);
    }

    /**
     * Sends collection usage when the namespace is executed without a concrete action.
     *
     * @param commandContext Command execution context.
     */
    @Override
    protected void executeSync(CommandContext commandContext) {
        reply(commandContext, Message.translation("server.commands.help.usage")
                .insert(":  (")
                .insert(Message.translation("server.commands.help.useHelpOnAnySubCommand"))
                .insert(")\n")
                .insert(getUsageString(commandContext.sender())));
    }

    /**
     * Uses compact collection usage, matching the SDK collection behavior.
     *
     * @param sender Sender that requested usage.
     * @return Usage message.
     */
    @Override
    public Message getUsageString(CommandSender sender) {
        return getUsageShort(sender, false);
    }

    /**
     * Marks this command as a namespace, not a no-argument action variant.
     *
     * @return False for namespaces.
     */
    public boolean isNoArgVariant() {
        return false;
    }
}
