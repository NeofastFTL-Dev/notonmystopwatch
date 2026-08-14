package com.neofastftl.nomsw.timing;

import com.mojang.logging.LogUtils;
import com.neofastftl.nomsw.Config;
import com.neofastftl.nomsw.Nomsw;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Writes timing results to the game log and to a dedicated file under
 * {@code logs/notonmystopwatch/timings.log}.
 */
public final class TimingLog {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault())
                    .withLocale(Locale.ROOT);

    private static Path logFile;
    private static Path gameDir;

    private TimingLog() {
    }

    public static void init(Path gameDirectory) {
        gameDir = gameDirectory;
        logFile = gameDirectory.resolve("logs").resolve("notonmystopwatch").resolve("timings.log");
    }

    public static void info(String message) {
        LOGGER.info("[{}] {}", Nomsw.MODID.toUpperCase(Locale.ROOT), message);
    }

    public static void warn(String message) {
        LOGGER.warn("[{}] {}", Nomsw.MODID.toUpperCase(Locale.ROOT), message);
    }

    /**
     * Logs a completed timing measurement to console and (optionally) file.
     *
     * @param category short label such as "BOOT" or "WORLD"
     * @param detail   human-readable details (world name, side, etc.)
     * @param watch    finished stopwatch
     */
    public static void record(String category, String detail, Stopwatch watch) {
        String duration = watch.formatElapsed();
        String line = String.format(
                Locale.ROOT,
                "%s | %s | duration=%s (%d ms) | startEpoch=%d | endEpoch=%d | %s",
                TIMESTAMP.format(Instant.ofEpochMilli(watch.getEndEpochMs() > 0
                        ? watch.getEndEpochMs()
                        : System.currentTimeMillis())),
                category,
                duration,
                watch.elapsedMillis(),
                watch.getStartEpochMs(),
                watch.getEndEpochMs(),
                detail
        );

        info(category + " complete: " + detail + " — " + duration + " (" + watch.elapsedMillis() + " ms)");
        appendToFile(line);
    }

    public static void note(String category, String message) {
        info(category + ": " + message);
        if (Config.writeLogFile) {
            String line = TIMESTAMP.format(Instant.now()) + " | " + category + " | " + message;
            appendToFile(line);
        }
    }

    private static void appendToFile(String line) {
        if (!Config.writeLogFile) {
            return;
        }
        try {
            Path target = logFile;
            if (target == null) {
                // Fallback if init() has not run yet (very early boot path).
                target = Path.of("logs", "notonmystopwatch", "timings.log");
            }
            Files.createDirectories(target.getParent());
            Files.writeString(
                    target,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            LOGGER.error("[{}] Failed to write timing log file", Nomsw.MODID.toUpperCase(Locale.ROOT), e);
        }
    }

    public static Path getLogFile() {
        return logFile;
    }

    public static Path getGameDir() {
        return gameDir;
    }
}
