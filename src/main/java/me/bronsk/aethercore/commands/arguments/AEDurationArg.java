package me.bronsk.aethercore.commands.arguments;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.arguments.types.SingleArgumentType;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import me.bronsk.aethercore.utils.AEMessageUtils;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class AEDurationArg extends SingleArgumentType<Duration> {

    public static final AEDurationArg INSTANCE = new AEDurationArg();

    private static final List<String> EXAMPLES = List.of("10m", "1h", "1d", "7d");

    private AEDurationArg() {
        super("duration", "Duration like 10m, 2h, 7d", "10m", "2h", "7d");
    }

    @Override
    public void suggest(@NonNullDecl CommandSender sender, @NonNullDecl String textAlreadyEntered,
                        int numParametersTyped, @NonNullDecl SuggestionResult result) {
        String lowerTextAlreadyEntered = textAlreadyEntered.toLowerCase(Locale.ROOT);
        for (String example : EXAMPLES) {
            if (example.startsWith(lowerTextAlreadyEntered)) {
                result.suggest(example);
            }
        }
    }

    @NullableDecl
    @Override
    public Duration parse(String value, ParseResult parseResult) {
        Duration parsed = tryParse(value);
        return parsed != null ? parsed : fail(value, parseResult);
    }

    /**
     * Parses a duration like {@code 10m}/{@code 2h}/{@code 7d} without command-parsing machinery.
     *
     * @param value Duration text.
     * @return Parsed duration, or null when {@code value} isn't a valid duration.
     */
    public static Duration tryParse(String value) {
        if (value == null || value.length() < 2) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        char unit = normalized.charAt(normalized.length() - 1);
        long amount;
        try {
            amount = Long.parseLong(normalized.substring(0, normalized.length() - 1));
        } catch (NumberFormatException exception) {
            return null;
        }

        if (amount <= 0) {
            return null;
        }

        return switch (unit) {
            case 's' -> Duration.ofSeconds(amount);
            case 'm' -> Duration.ofMinutes(amount);
            case 'h' -> Duration.ofHours(amount);
            case 'd' -> Duration.ofDays(amount);
            default -> null;
        };
    }

    /**
     * Formats a duration back into a {@code tryParse}-compatible string, using the largest unit
     * that still divides it evenly (days, then hours, minutes, seconds).
     *
     * @param duration Duration to format, or null.
     * @return Formatted text, or {@code ""} when {@code duration} is null.
     */
    public static String format(Duration duration) {
        if (duration == null) {
            return "";
        }

        long seconds = duration.getSeconds();
        if (seconds <= 0) {
            return "";
        }
        if (seconds % 86400 == 0) {
            return (seconds / 86400) + "d";
        }
        if (seconds % 3600 == 0) {
            return (seconds / 3600) + "h";
        }
        if (seconds % 60 == 0) {
            return (seconds / 60) + "m";
        }
        return seconds + "s";
    }

    private Duration fail(String value, ParseResult parseResult) {
        parseResult.fail(Message.empty()
                .insert(AEMessageUtils.fromColoredString("&7Invalid duration &6" + value
                        + "&7. Use values like &610m&7, &62h&7, or &67d")));
        return null;
    }
}
