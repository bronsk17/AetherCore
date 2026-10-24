package me.bronsk.aethercore.ui.features.overview;

import com.hypixel.hytale.server.core.universe.Universe;
import me.bronsk.aethercore.ui.components.AEMetricCard;
import me.bronsk.aethercore.ui.framework.AEUIComponent;
import me.bronsk.aethercore.utils.AEConsoleLogBuffer;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * The Overview tab: 3 top metric cards (players online, RAM, CPU) and a read-only tail of the
 * server console below - newest line first, since this native UI framework has no way to force a
 * scroll position, so "always see the latest" is achieved by ordering instead of scrolling.
 * Refreshes itself every {@link #REFRESH_INTERVAL_SECONDS} seconds via {@link #autoRefresh} - see
 * {@code AEUIRefreshScheduler} for how "only the latest instance per admin keeps ticking" works.
 */
public final class AEUIOverviewView extends AEUIComponent {

    private static final String TEMPLATE = "Pages/AetherCore/Features/Overview/AEUIOverviewView.ui";
    private static final Duration REFRESH_INTERVAL = Duration.ofSeconds(5);

    @Override
    protected String getTemplate() {
        return TEMPLATE;
    }

    @Override
    protected Map<String, Object> getContent() {
        autoRefresh(REFRESH_INTERVAL, "Cards", "ConsoleCard.Console");
        double ramPercent = ramUsedPercent();
        Double cpuPercent = cpuUsedPercent();
        return Map.of(
                "Cards", List.of(
                        new AEMetricCard(String.valueOf(Universe.get().getPlayerCount()), lang("ui.overview.players_online")),
                        new AEMetricCard(formatPercent(ramPercent), thresholdColor(ramPercent), lang("ui.overview.ram_used")),
                        new AEMetricCard(cpuPercent == null ? lang("ui.common.not_available") : formatPercent(cpuPercent), cpuPercent == null ? null : thresholdColor(cpuPercent), lang("ui.overview.cpu_usage"))
                ),
                "ConsoleCard.Console", consoleLines()
        );
    }

    /**
     * One fixed-height row per line, never wrapped - a wrapped Label wouldn't grow its row to fit,
     * so a long line would visually overlap the row below it. Truncating instead guarantees every
     * row stays exactly one line tall.
     */
    private List<AEUIComponent> consoleLines() {
        List<AEConsoleLogBuffer.Line> lines = AEConsoleLogBuffer.getRecentLines();
        List<AEConsoleLogBuffer.Line> newestFirst = new ArrayList<>(lines);
        Collections.reverse(newestFirst);
        return newestFirst.stream().map(line -> (AEUIComponent) new AEConsoleLine(truncate(line.text()), colorForLevel(line.level()))).toList();
    }

    private static String truncate(String text) {
        // Plain ASCII "..." on purpose - the game font renders unicode ellipsis as "?" (tofu),
        // same reason AETable's sort arrows use "^"/"v" instead of real arrow glyphs.
        int maxChars = 160;
        return text.length() > maxChars ? text.substring(0, maxChars) + "..." : text;
    }

    /** Mirrors the real console's own level->color mapping (decompiled from {@code HytaleLogFormatter}: ALL/FINEST=cyan, FINER=blue, FINE=magenta, CONFIG=green, INFO=default, WARNING=yellow, SEVERE=red). */
    private static String colorForLevel(Level level) {
        if (level.equals(Level.SEVERE)) {
            return "#ff5555";
        }
        if (level.equals(Level.WARNING)) {
            return "#ffff55";
        }
        if (level.equals(Level.CONFIG)) {
            return "#55ff55";
        }
        if (level.equals(Level.FINE)) {
            return "#ff55ff";
        }
        if (level.equals(Level.FINER)) {
            return "#5555ff";
        }
        if (level.equals(Level.FINEST) || level.equals(Level.ALL)) {
            return "#55ffff";
        }
        return null;
    }

    private static double ramUsedPercent() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        return used * 100.0 / runtime.maxMemory();
    }

    /** Null when the JVM doesn't expose process CPU load (non-HotSpot, or not yet sampled). */
    private static Double cpuUsedPercent() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
            double load = sunBean.getProcessCpuLoad();
            if (load >= 0) {
                return load * 100;
            }
        }
        return null;
    }

    private static String formatPercent(double percent) {
        return String.format(Locale.ROOT, "%.0f%%", percent);
    }

    /** White below 70%, yellow from 70% to 90%, red at 90% and above. */
    private static String thresholdColor(double percent) {
        if (percent >= 90) {
            return "#ff5555";
        }
        if (percent >= 70) {
            return "#ffff55";
        }
        return "#ffffff";
    }
}
