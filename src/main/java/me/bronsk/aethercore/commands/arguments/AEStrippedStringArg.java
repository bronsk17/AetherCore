package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class AEStrippedStringArg extends SingleArgumentType<String> {

    public static final AEStrippedStringArg INSTANCE = new AEStrippedStringArg();

    private AEStrippedStringArg() {
        super("stripped_string", "Quoted string with surrounding quotes removed");
    }

    @NullableDecl
    @Override
    public String parse(String value, ParseResult parseResult) {
        if (value == null || value.length() < 2) {
            return value;
        }

        char first = value.charAt(0);
        char last = value.charAt(value.length() - 1);
        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }

    @Override
    public boolean isGreedyString() {
        return true;
    }
}
