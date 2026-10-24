package me.bronsk.aethercore.modules.permissions.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import me.bronsk.aethercore.commands.AECommand;
import me.bronsk.aethercore.modules.AEModulesManager;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;
import me.bronsk.aethercore.modules.permissions.AEPermissionsPerms;

/** Lists the caller's own direct, group, and inherited permissions, grouped by group and priority. */
public class AEPermListCommand extends AECommand {

    public AEPermListCommand() {
        super("list", "server.commands.perm.list.desc");
        requirePermission(AEPermissionsPerms.LIST);
    }

    @Override
    protected void executeSync(CommandContext commandContext) {
        AEPermissionsModule tcPermissionsModule = AEModulesManager.get().getModule(AEPermissionsModule.class);
        reply(commandContext, tcPermissionsModule.getUsersModule().handleListEffectivePermissions(getExecutionContext(commandContext)));
    }
}
