package me.bronsk.aethercore.modules.permissions;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import com.j256.ormlite.support.ConnectionSource;
import me.bronsk.aethercore.AELogger;
import me.bronsk.aethercore.AetherPlugin;
import me.bronsk.aethercore.database.AEDatabaseUtils;
import me.bronsk.aethercore.database.providers.AEDatabaseDialect;
import me.bronsk.aethercore.modules.AEExecutionContext;
import me.bronsk.aethercore.modules.AEModule;
import me.bronsk.aethercore.modules.lang.AELangModule;
import me.bronsk.aethercore.modules.permissions.storage.AEPermissionsSqlStorage;
import me.bronsk.aethercore.modules.permissions.storage.AEPermissionsStorage;
import me.bronsk.aethercore.modules.permissions.commands.AEPermCommand;
import me.bronsk.aethercore.modules.permissions.defaults.AEDefaultPermissionGroups;
import me.bronsk.aethercore.modules.player.AEPlayersModule;
import me.bronsk.aethercore.text.AETextPlaceholders;
import me.bronsk.aethercore.utils.AEScheduler;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.bronsk.aethercore.modules.lang.AELangModule.args;

/**
 * Bootstraps the permissions feature - a concern that belongs to neither a specific user nor a
 * specific group. Everything user-centric lives in {@link AEPermissionsUsersModule}, everything
 * group-centric in {@link AEPermissionsGroupsModule}; both are sub-modules reachable via
 * {@link #getUsersModule()}/{@link #getGroupsModule()}, the same pattern already established by
 * {@link AEGroupSettingsModule}/{@link #getGroupSettingsModule()}.
 */
public class AEPermissionsModule extends AEModule<AEPermissionsConfig, AEPermissionsStorage> {

    public static final String NAME = "permissions";

    /**
     * Creates the permissions module.
     */
    private volatile boolean cleanupTaskActive;

    public AEPermissionsModule() {
        addSubModule(new AEGroupSettingsModule());
        addSubModule(new AEPermissionsUsersModule());
        addSubModule(new AEPermissionsGroupsModule());
    }

    @Override
    protected void onInit() {
        getModulesManager().getDatabaseInstaller().registerInstaller(new AEDefaultPermissionGroups(this));
        registerDefaultTextPlaceholders();

        PermissionsModule permissions = PermissionsModule.get();
        PermissionProvider firstProvider = permissions.getFirstPermissionProvider();
        if (firstProvider != null) {
            permissions.removeProvider(firstProvider);
        }
        permissions.addProvider(new AEPermissionsProvider(this));

        AetherPlugin.getPlugin().getRootCommand().addSubCommand(new AEPermCommand());

        startExpiredCleanupTaskIfEnabled();
    }

    @Override
    protected void onClose() throws Exception {
        cleanupTaskActive = false;
        super.onClose();
    }

    /**
     * An expired grant/membership is already excluded from resolution regardless of this task (see
     * {@link AEPermissionsUsersModule#getRuntimeUserPermissions}/{@link
     * AEPermissionsGroupsModule#getRuntimeGroupPermissions}) - this only sweeps the now-inert rows
     * out of the database every {@code clearExpiredPermsAndGroupsAfter} seconds, instead of letting
     * them linger forever as history. Disabled when that config value is {@code <= 0}.
     */
    private void startExpiredCleanupTaskIfEnabled() {
        int intervalSeconds = getConfig().getClearExpiredPermsAndGroupsAfter();
        if (intervalSeconds <= 0) {
            return;
        }
        cleanupTaskActive = true;
        scheduleCleanupTick(intervalSeconds);
    }

    private void scheduleCleanupTick(int intervalSeconds) {
        AEScheduler.schedule(() -> runCleanupTick(intervalSeconds), intervalSeconds, TimeUnit.SECONDS);
    }

    /** Runs on {@link AEScheduler}'s own thread, not the world tick thread - only touches the database, never game/UI state, so (unlike a UI auto-refresh) no thread-hop back is needed here. */
    private void runCleanupTick(int intervalSeconds) {
        if (!cleanupTaskActive) {
            return;
        }
        try {
            int removedPermissions = getStorage().deleteExpiredPlayerPermissions();
            int removedMemberships = getStorage().deleteExpiredPlayerGroups();
            int removedGroupPermissions = getStorage().deleteExpiredGroupPermissions();
            int total = removedPermissions + removedMemberships + removedGroupPermissions;
            if (total > 0) {
                AELogger.logInfo("Permissions cleanup: removed %d expired direct grant(s), %d expired group membership(s), %d expired group grant(s)",
                        removedPermissions, removedMemberships, removedGroupPermissions);
            }
        } catch (Exception exception) {
            AELogger.logException("Permissions expired-rows cleanup failed", exception);
        }
        scheduleCleanupTick(intervalSeconds);
    }

    @Override
    protected List<Class<? extends AEModule<?, ?>>> getRequiredModules() {
        return List.of(AEPlayersModule.class);
    }

    private void registerDefaultTextPlaceholders() {
        AETextPlaceholders.get().register("prefix",
                context -> context.player() == null ? "" : getGroupSettingsModule().getPlayerPrefix(context.player().getUuid()));
        AETextPlaceholders.get().register("suffix",
                context -> context.player() == null ? "" : getGroupSettingsModule().getPlayerSuffix(context.player().getUuid()));
    }

    public AEGroupSettingsModule getGroupSettingsModule() {
        return getSubModule(AEGroupSettingsModule.class);
    }

    public AEPermissionsUsersModule getUsersModule() {
        return getSubModule(AEPermissionsUsersModule.class);
    }

    public AEPermissionsGroupsModule getGroupsModule() {
        return getSubModule(AEPermissionsGroupsModule.class);
    }

    /**
     * Creates permissions storage from the configured backend.
     */
    @Override
    protected AEPermissionsStorage createStorage(ConnectionSource connectionSource, AEDatabaseDialect dialect) throws Exception {
        return new AEPermissionsSqlStorage(connectionSource, dialect);
    }

    /**
     * Returns the stable module name.
     *
     * @return Module name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /** Every player UUID with a permissions footprint (a direct grant or a group membership) - not filtered by any specific node. */
    public Set<String> getPermissionCandidateUuids() {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getPermissionCandidateUuids());
    }

    /** Every distinct permission string currently granted to some group or some player - suggested when granting a new one. */
    public Set<String> getAllKnownPermissions() {
        return AEDatabaseUtils.supplyUnchecked(() -> getStorage().getAllKnownPermissions());
    }

    /** Shared by {@link AEPermissionsUsersModule#handleGetUserGroups} and {@link AEPermissionsGroupsModule#handleGetGroupPermissions} - lists one value set per registered {@link PermissionProvider}. */
    Message handleGetProviderList(AEExecutionContext context, Function<PermissionProvider, Set<String>> valuesProvider, String titleCode) {
        Message response = Message.empty();
        boolean firstProvider = true;
        for (PermissionProvider provider : PermissionsModule.get().getProviders()) {
            if (!firstProvider) {
                response.insert("\n");
            }
            firstProvider = false;
            Set<Message> values = valuesProvider.apply(provider).stream()
                    .map(Message::raw)
                    .collect(Collectors.toSet());
            response.insert(MessageFormat.list(getLangModule().buildMessage(titleCode, context, args("provider", provider.getName())), values));
        }
        return response;
    }

    AELangModule getLangModule() {
        return getModulesManager().getModule(AELangModule.class);
    }
}
