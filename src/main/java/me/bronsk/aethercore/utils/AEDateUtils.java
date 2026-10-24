package me.bronsk.aethercore.utils;

import me.bronsk.aethercore.database.storage.config.AEGlobalConfig;
import me.bronsk.aethercore.modules.AEModulesManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class AEDateUtils {

    private AEDateUtils() {
    }

    /**
     * Returns the current instant as a SQL timestamp - the shared "now" for anything stamping a
     * created/updated column.
     *
     * @return Current timestamp.
     */
    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    /**
     * Formats a timestamp for display. Callers read the pattern to pass from
     * {@code AEModulesManager.get().getGlobalConfig().getDateTimeFormat()} - this method takes no
     * dependency on the module system itself, it just applies whatever {@link DateTimeFormatter}
     * pattern it's given.
     *
     * @param timestamp Timestamp to format, or null.
     * @param format {@link DateTimeFormatter} pattern (e.g. "yyyy-MM-dd HH:mm:ss").
     * @return Formatted date, or an empty string when {@code timestamp} is null.
     */
    public static String format(Timestamp timestamp, String format) {
        if (timestamp == null) {
            return "";
        }
        if (format == null) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        return DateTimeFormatter.ofPattern(format).format(timestamp.toLocalDateTime());
    }

    /**
     * Formats a timestamp for display based on the modules manager global config.
     *
     * @param timestamp Timestamp to format, or null.
     * @return Formatted date, or an empty string when {@code timestamp} is null.
     */
    public static String formatByConfig(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        String format = AEModulesManager.get().getGlobalConfig().getDateTimeFormat();
        if (format == null) {
            format = new AEGlobalConfig().getDateTimeFormat();
        }
        return format(timestamp, format);
    }
}
