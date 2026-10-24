package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import me.bronsk.aethercore.utils.AEMessageUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;
import java.util.Set;

public class AEGroupTypeArg extends SingleArgumentType<String> {

    public static final AEGroupTypeArg INSTANCE = new AEGroupTypeArg();

    private AEGroupTypeArg() {
        super("group", "Permission group", "default", "Admin", "hytale:Admin");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered, int numParametersTyped, @NonNullDecl SuggestionResult result) {
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase();
        for (String group : getRegisteredGroups()) {
            if (group.toLowerCase().startsWith(lowerTextAlreadyEntered)) {
                result.suggest(group);
            }
        }
    }

    @NullableDecl
    @Override
    public String parse(String s, ParseResult parseResult) {
        String group = resolveGroup(s);
        if (group != null) {
            return group;
        }

        List<String> groups = List.copyOf(getRegisteredGroups());
        parseResult.fail(
                Message.empty().insert(AEMessageUtils.fromColoredString("&7Unknown permission group &6" +s)).insert(Message.raw("")).insert(Message.translation("server.general.failed.didYouMean").param("choices", StringUtil.sortByFuzzyDistance(s.toLowerCase(), groups, CommandUtil.RECOMMEND_COUNT).toString())));
        return null;
    }

    @NullableDecl
    private static String resolveGroup(String groupName) {
        Set<String> groups = getRegisteredGroups();
        if (groups.contains(groupName)) {
            return groupName;
        }

        for (String group : groups) {
            if (group.equalsIgnoreCase(groupName)) {
                return group;
            }
        }

        return null;
    }

    private static Set<String> getRegisteredGroups() {
        return PermissionsModule.get().getAllRegisteredGroups();
    }
}
