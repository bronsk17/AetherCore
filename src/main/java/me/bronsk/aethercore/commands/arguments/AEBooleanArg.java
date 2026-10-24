package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import me.bronsk.aethercore.utils.AEMessageUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.List;
import java.util.Locale;

public class AEBooleanArg extends SingleArgumentType<Boolean> {

    public static final AEBooleanArg INSTANCE = new AEBooleanArg();

    private static final List<String> VALUES = List.of("true", "false", "yes", "no", "1", "0");

    private AEBooleanArg() {
        super("boolean", "Boolean value", "true", "false", "yes", "no", "1", "0");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered,
                        int numParametersTyped, @NonNullDecl SuggestionResult result) {
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase(Locale.ROOT);
        for (String value : VALUES) {
            if (value.startsWith(lowerTextAlreadyEntered)) {
                result.suggest(value);
            }
        }
    }

    @NullableDecl
    @Override
    public Boolean parse(String value, ParseResult parseResult) {
        if (value == null) {
            return fail(value, parseResult);
        }

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "true", "yes", "1" -> true;
            case "false", "no", "0" -> false;
            default -> fail(value, parseResult);
        };
    }

    private Boolean fail(String value, ParseResult parseResult) {
        parseResult.fail(Message.empty()
                .insert(AEMessageUtils.fromColoredString("&7Invalid boolean &6" + value
                        + "&7. Use &6true&7, &6false&7, &6yes&7, &6no&7, &61&7, or &60")));
        return null;
    }
}
