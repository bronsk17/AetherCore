package me.bronsk.aethercore.modules.player.arguments;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Locale;

public class AEPlayerArg extends SingleArgumentType<String> {

    public static final AEPlayerArg INSTANCE = new AEPlayerArg();

    private AEPlayerArg() {
        super("player", "UUID or username", "Notch");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered,
                        int numParametersTyped, @NonNullDecl SuggestionResult result) {
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase(Locale.ROOT);
        for (PlayerRef playerRef : Universe.get().getPlayers()) {
            String username = playerRef.getUsername();
            if (username != null && username.toLowerCase(Locale.ROOT).startsWith(lowerTextAlreadyEntered)) {
                result.suggest(username);
            }
        }
    }

    @NullableDecl
    @Override
    public String parse(String value, ParseResult parseResult) {
        return value;
    }
}
