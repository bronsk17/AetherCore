package me.bronsk.aethercore.modules.permissions;

/**
 * Every permission node owned by the Permissions module - generic capability nodes, not tied to
 * how they're invoked (a {@code /aether permissions} command today, a UI control tomorrow). A setting exposed
 * as get/set/reset shares one node across all three verbs.
 */
public final class AEPermissionsPerms {

    public static final String GROUP_LIST = "tc.permissions.group.list";
    public static final String GROUP_CREATE = "tc.permissions.group.create";
    public static final String GROUP_DELETE = "tc.permissions.group.delete";
    public static final String GROUP_ADD = "tc.permissions.group.add";
    public static final String GROUP_REMOVE = "tc.permissions.group.remove";
    public static final String GROUP_CHILDREN_ADD = "tc.permissions.group.children.add";
    public static final String GROUP_CHILDREN_REMOVE = "tc.permissions.group.children.remove";
    public static final String GROUP_CHILDREN_LIST = "tc.permissions.group.children.list";
    public static final String GROUP_DEFAULT = "tc.permissions.group.default";
    public static final String GROUP_PREFIX = "tc.permissions.group.prefix";
    public static final String GROUP_SUFFIX = "tc.permissions.group.suffix";
    public static final String GROUP_PRIORITY = "tc.permissions.group.priority";

    public static final String USER_LIST = "tc.permissions.user.list";
    public static final String USER_ADD = "tc.permissions.user.add";
    public static final String USER_REMOVE = "tc.permissions.user.remove";
    public static final String USER_GROUP_LIST = "tc.permissions.user.group.list";
    public static final String USER_GROUP_ADD = "tc.permissions.user.group.add";
    public static final String USER_GROUP_REMOVE = "tc.permissions.user.group.remove";

    public static final String LIST = "tc.permissions.list";

    private AEPermissionsPerms() {
    }
}
