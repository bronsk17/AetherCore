package me.bronsk.aethercore.modules.permissions;

import me.bronsk.aethercore.database.storage.config.AEModuleConfig;

public class AEPermissionsConfig extends AEModuleConfig {

    /**
     * Seconds between background sweeps that physically delete already-expired permission grants
     * and group memberships - {@code -1} disables the sweep entirely. An expired row is already
     * excluded from permission resolution regardless of this setting (see {@code
     * AEPermissionsUsersModule#getRuntimeUserPermissions}/{@code
     * AEPermissionsGroupsModule#getRuntimeGroupPermissions}); this only controls whether/how often
     * the now-inert rows get cleaned out of the database instead of lingering forever as history.
     */
    private int clearExpiredPermsAndGroupsAfter = -1;

    public AEPermissionsConfig() {
    }

    public int getClearExpiredPermsAndGroupsAfter() {
        return clearExpiredPermsAndGroupsAfter;
    }

    public void setClearExpiredPermsAndGroupsAfter(int clearExpiredPermsAndGroupsAfter) {
        this.clearExpiredPermsAndGroupsAfter = clearExpiredPermsAndGroupsAfter;
    }
}
