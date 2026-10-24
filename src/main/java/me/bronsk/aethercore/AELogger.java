package me.bronsk.aethercore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

public final class AELogger {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Creates a new instance.
     */
    private AELogger() {
    }

    /**
     * Handles logInfo.
     *
     * @param message Method argument.
     */
    public static void logInfo(String message) {
        log(Level.INFO, message);
    }

    /**
     * Handles logWarning.
     *
     * @param message Method argument.
     */
    public static void logWarning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Handles logError.
     *
     * @param message Method argument.
     */
    public static void logError(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Handles logDebug.
     *
     * @param message Method argument.
     */
    public static void logDebug(String message) {
        log(Level.FINE, message);
    }

    /**
     * Handles logInfo.
     *
     * @param format Method argument.
     * @param args Method argument.
     */
    public static void logInfo(String format, Object... args) {
        logInfo(format(format, args));
    }

    /**
     * Handles logWarning.
     *
     * @param format Method argument.
     * @param args Method argument.
     */
    public static void logWarning(String format, Object... args) {
        logWarning(format(format, args));
    }

    /**
     * Handles logError.
     *
     * @param format Method argument.
     * @param args Method argument.
     */
    public static void logError(String format, Object... args) {
        logError(format(format, args));
    }

    /**
     * Handles logDebug.
     *
     * @param format Method argument.
     * @param args Method argument.
     */
    public static void logDebug(String format, Object... args) {
        logDebug(format(format, args));
    }

    /**
     * Handles logException.
     *
     * @param e Method argument.
     */
    public static void logException(Exception e) {
        logException(null, e);
    }

    /**
     * Handles logException.
     *
     * @param throwable Method argument.
     */
    public static void logException(Throwable throwable) {
        logException(null, throwable);
    }

    /**
     * Handles logException.
     *
     * @param message Method argument.
     * @param e Method argument.
     */
    public static void logException(String message, Exception e) {
        logException(message, (Throwable) e);
    }

    /**
     * Handles logException.
     *
     * @param message Method argument.
     * @param throwable Method argument.
     */
    public static void logException(String message, Throwable throwable) {
        if (message != null && !message.isBlank()) {
            logError(message);
        }

        if (throwable == null) {
            logError("Exception was null");
            return;
        }

        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        for (String line : stringWriter.toString().split("\\R")) {
            log(Level.SEVERE, line);
        }
    }

    /**
     * Handles logJson.
     *
     * @param label Method argument.
     * @param object Method argument.
     */
    public static void logJson(String label, Object object) {
        if (label != null && !label.isBlank()) {
            logInfo(label);
        }
        log(Level.INFO, GSON.toJson(object));
    }

    /**
     * Handles log.
     *
     * @param level Method argument.
     * @param message Method argument.
     */
    public static void log(Level level, String message) {
        Level effectiveLevel = level == null ? Level.INFO : level;
        String formattedMessage = formatMessage(message);

        try {
            HytaleLogger.Api logger = AetherPlugin.getPlugin().getLogger().at(effectiveLevel);
            logger.log(formattedMessage);
        } catch (Exception ignored) {
            logFallback(effectiveLevel, formattedMessage);
        }
    }

    /**
     * Handles format.
     *
     * @param format Method argument.
     * @param args Method argument.
     * @return Method result.
     */
    private static String format(String format, Object... args) {
        if (format == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return format;
        }
        return String.format(format, args);
    }

    /**
     * Handles formatMessage.
     *
     * @param message Method argument.
     * @return Method result.
     */
    private static String formatMessage(String message) {
        return message == null ? "" : message;
    }

    /**
     * Handles logFallback.
     *
     * @param level Method argument.
     * @param message Method argument.
     */
    private static void logFallback(Level level, String message) {
        if (level.intValue() >= Level.WARNING.intValue()) {
            System.err.println(message);
            return;
        }
        System.out.println(message);
    }
}
