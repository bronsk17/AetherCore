package me.bronsk.aethercore.modules.permissions;

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class AEPermissionsProvider implements PermissionProvider {

    private final AEPermissionsModule permissionsModule;

    /**
     * Creates a new instance.
     *
     * @param permissionsModule Method argument.
     */
    public AEPermissionsProvider(AEPermissionsModule permissionsModule) {
        this.permissionsModule = permissionsModule;
    }

    /**
     * Returns getName.
     *
     * @return Method result.
     */
    @NonNullDecl
    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Adds data through addUserPermissions.
     *
     * @param uuid Method argument.
     * @param set Method argument.
     */
    @Override
    public void addUserPermissions(@NonNullDecl UUID uuid, @NonNullDecl Set<String> set) {
        permissionsModule.getUsersModule().addUserPermissions(uuid, set);
    }

    /**
     * Removes data through removeUserPermissions.
     *
     * @param uuid Method argument.
     * @param set Method argument.
     */
    @Override
    public void removeUserPermissions(@NonNullDecl UUID uuid, @NonNullDecl Set<String> set) {
        permissionsModule.getUsersModule().removeUserPermissions(uuid, set);
    }

    /**
     * The set the SDK's own resolver actually checks against - world/expiry-restricted direct
     * grants are filtered out here (live, not cached against the clock/current world) - see {@link
     * AEPermissionsUsersModule#getRuntimeUserPermissions}.
     *
     * @param uuid Method argument.
     * @return Method result.
     */
    @Override
    public Set<String> getUserPermissions(@NonNullDecl UUID uuid) {
        return permissionsModule.getUsersModule().getRuntimeUserPermissions(uuid);
    }

    /**
     * Adds data through addGroupPermissions.
     *
     * @param s Method argument.
     * @param set Method argument.
     */
    @Override
    public void addGroupPermissions(@NonNullDecl String s, @NonNullDecl Set<String> set) {
        permissionsModule.getGroupsModule().addGroupPermissions(s, set);
    }

    /**
     * Removes data through removeGroupPermissions.
     *
     * @param s Method argument.
     * @param set Method argument.
     */
    @Override
    public void removeGroupPermissions(@NonNullDecl String s, @NonNullDecl Set<String> set) {
        permissionsModule.getGroupsModule().removeGroupPermissions(s, set);
    }

    /**
     * The set the SDK's own resolver actually checks against - currently-expired grants are filtered
     * out here (live, not cached against the clock) - see {@link
     * AEPermissionsGroupsModule#getRuntimeGroupPermissions}.
     *
     * @param s Method argument.
     * @return Method result.
     */
    @Override
    public Set<String> getGroupPermissions(@NonNullDecl String s) {
        return permissionsModule.getGroupsModule().getRuntimeGroupPermissions(s);
    }

    /**
     * Adds data through addUserToGroup.
     *
     * @param uuid Method argument.
     * @param s Method argument.
     */
    @Override
    public void addUserToGroup(@NonNullDecl UUID uuid, @NonNullDecl String s) {
        permissionsModule.getUsersModule().addUserToGroup(uuid, s);
    }

    /**
     * Removes data through removeUserFromGroup.
     *
     * @param uuid Method argument.
     * @param s Method argument.
     */
    @Override
    public void removeUserFromGroup(@NonNullDecl UUID uuid, @NonNullDecl String s) {
        permissionsModule.getUsersModule().removeUserFromGroup(uuid, s);
    }

    /**
     * Returns getGroupsForUser.
     *
     * @param uuid Method argument.
     * @return Method result.
     */
    @Override
    public Set<String> getGroupsForUser(@NonNullDecl UUID uuid) {
        return permissionsModule.getUsersModule().getGroupsForUser(uuid);
    }

    /**
     * Sets setUserGroup.
     *
     * @param uuid Method argument.
     * @param s Method argument.
     */
    @Override
    public void setUserGroup(@NonNullDecl UUID uuid, @NonNullDecl String s) {
        permissionsModule.getUsersModule().setUserGroup(uuid, s);
    }

    /**
     * A group can inherit from any number of other groups now (see {@link
     * me.bronsk.aethercore.modules.permissions.AEPermissionsGroupsModule#getGroupChildren}), so
     * there's no single answer this SDK interface method (which expects at most one) could give -
     * always null.
     *
     * @param s Method argument.
     * @return Always null.
     */
    @NullableDecl
    @Override
    public String getGroupParent(@NonNullDecl String s) {
        return null;
    }

    /**
     * Returns getAllRegisteredGroups.
     *
     * @return Method result.
     */
    @NonNullDecl
    @Override
    public Set<String> getAllRegisteredGroups() {
        return permissionsModule.getGroupsModule().getAllRegisteredGroups();
    }

    /**
     * Returns getEffectiveGroupPermissions.
     *
     * @param s Method argument.
     * @return Method result.
     */
    @NonNullDecl
    @Override
    public Set<String> getEffectiveGroupPermissions(@NonNullDecl String s) {
        return permissionsModule.getGroupsModule().getEffectiveGroupPermissions(s);
    }

    /**
     * Every player with a raw permission set (direct grants, or via a group - including inherited
     * ancestors) that resolves {@code permission} to granted, using the same negation-first,
     * specific-before-general wildcard precedence the SDK's own {@code hasPermission} applies. Only
     * considers players with an explicit permissions/groups footprint - a player who has never been
     * touched and only implicitly sits in the default group isn't enumerated here.
     */
    @NonNullDecl
    @Override
    public Set<UUID> getUsersWithPermission(@NonNullDecl String permission) {
        Set<UUID> matches = new LinkedHashSet<>();
        for (String candidateUuid : permissionsModule.getPermissionCandidateUuids()) {
            UUID uuid;
            try {
                uuid = UUID.fromString(candidateUuid);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (resolvesGranted(permissionsModule.getUsersModule().getUserPermissions(uuid), permission)) {
                matches.add(uuid);
            }
        }
        return matches;
    }

    /** Same negation-before-grant, specific-before-general precedence the SDK's own resolver applies to a raw permission set. */
    private static boolean resolvesGranted(Set<String> rawPermissions, String node) {
        if (rawPermissions.contains("-*")) {
            return false;
        }
        if (rawPermissions.contains("-" + node)) {
            return false;
        }
        if (rawPermissions.contains(node)) {
            return true;
        }

        List<String> prefixes = getWildcardPrefixesMostSpecificFirst(node);
        for (String prefix : prefixes) {
            if (rawPermissions.contains("-" + prefix + ".*")) {
                return false;
            }
        }
        if (rawPermissions.contains("*")) {
            return true;
        }
        for (String prefix : prefixes) {
            if (rawPermissions.contains(prefix + ".*")) {
                return true;
            }
        }
        return false;
    }

    /** e.g. "bc.commands.foo" -> ["bc.commands", "bc"]. */
    private static List<String> getWildcardPrefixesMostSpecificFirst(String node) {
        List<String> prefixes = new ArrayList<>();
        int lastDot = node.lastIndexOf('.');
        while (lastDot >= 0) {
            prefixes.add(node.substring(0, lastDot));
            lastDot = node.lastIndexOf('.', lastDot - 1);
        }
        return prefixes;
    }
}
