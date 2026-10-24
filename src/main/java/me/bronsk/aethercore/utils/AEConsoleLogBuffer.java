package me.bronsk.aethercore.utils;

import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A bounded, always-on tap into every log line the server's logging backend emits, via {@link
 * HytaleLoggerBackend#subscribe} - backs the Overview tab's read-only console panel. Call {@link
 * #start()} once at plugin startup and {@link #stop()} at shutdown.
 */
public final class AEConsoleLogBuffer {

    private static final int MAX_LINES = 300;
    private static final CopyOnWriteArrayList<LogRecord> RECORDS = new CopyOnWriteArrayList<>();
    private static volatile boolean started;

    private AEConsoleLogBuffer() {
    }

    public static synchronized void start() {
        if (started) {
            return;
        }
        HytaleLoggerBackend.subscribe(RECORDS);
        started = true;
    }

    public static synchronized void stop() {
        if (!started) {
            return;
        }
        HytaleLoggerBackend.unsubscribe(RECORDS);
        started = false;
    }

    /** The buffered log lines, oldest first - trims the underlying buffer down to {@link #MAX_LINES} as a side effect. */
    public static List<Line> getRecentLines() {
        int overflow = RECORDS.size() - MAX_LINES;
        for (int i = 0; i < overflow && !RECORDS.isEmpty(); i++) {
            RECORDS.remove(0);
        }
        return RECORDS.stream().map(record -> new Line(record.getMessage() == null ? "" : record.getMessage(), record.getLevel())).toList();
    }

    /** One buffered line, still carrying its {@link Level} so the UI can color it the same way the real console does. */
    public record Line(String text, Level level) {
    }
}
