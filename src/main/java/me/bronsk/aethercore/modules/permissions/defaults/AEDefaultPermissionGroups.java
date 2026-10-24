package me.bronsk.aethercore.modules.permissions.defaults;

import me.bronsk.aethercore.database.ITCDefaultInstaller;
import me.bronsk.aethercore.modules.permissions.AEPermissionsModule;

import java.sql.SQLException;
import java.util.Objects;

public final class AEDefaultPermissionGroups implements ITCDefaultInstaller {

    public static final String DEFAULT_GROUP = "default";

    private final AEPermissionsModule permissionsModule;

    public AEDefaultPermissionGroups(AEPermissionsModule permissionsModule) {
        this.permissionsModule = Objects.requireNonNull(permissionsModule, "permissionsModule");
    }

    @Override
    public void install() throws SQLException {
        permissionsModule.getGroupsModule().installDefaultGroup(DEFAULT_GROUP);
    }
}
