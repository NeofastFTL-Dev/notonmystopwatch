package com.neofastftl.nomsw.timing;

import com.neofastftl.nomsw.Config;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Measures time from JVM process start until the game is considered fully booted
 * (client title screen first shown, or dedicated server finished starting).
 */
public final class BootTimer {

    private static final AtomicBoolean FINISHED = new AtomicBoolean(false);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private static volatile Stopwatch watch;
    private static volatile String sideLabel = "unknown";
    private static volatile String formattedDuration;

    private BootTimer() {
    }

    /**
     * Marks the earliest possible start. Safe to call multiple times; only the
     * first call wins. Uses the JVM process start time as the true boot origin.
     */
    public static void markStart(String side) {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        sideLabel = side;
        long jvmStart = ManagementFactory.getRuntimeMXBean().getStartTime();
        watch = Stopwatch.startFromEpochMs(jvmStart);
        TimingLog.note("BOOT", "Stopwatch started from JVM process start (" + side + ")");
    }

    /**
     * Completes the boot measurement once. Subsequent calls are ignored.
     *
     * @param reason short description of what triggered completion
     * @return formatted duration, or null if already finished / not started
     */
    public static String markFinished(String reason) {
        if (!Config.measureBoot) {
            return null;
        }
        if (!STARTED.get()) {
            markStart(sideLabel);
        }
        if (!FINISHED.compareAndSet(false, true)) {
            return null;
        }
        Stopwatch w = watch;
        if (w == null) {
            return null;
        }
        w.stop();
        formattedDuration = w.formatElapsed();
        String detail = "side=" + sideLabel + " | readyWhen=" + reason;
        TimingLog.record("BOOT", detail, w);
        return formattedDuration;
    }

    public static boolean isFinished() {
        return FINISHED.get();
    }

    /** Formatted boot duration after finish, or null if not finished yet. */
    public static String getFormattedDuration() {
        return formattedDuration;
    }

    public static Stopwatch getWatch() {
        return watch;
    }
}
