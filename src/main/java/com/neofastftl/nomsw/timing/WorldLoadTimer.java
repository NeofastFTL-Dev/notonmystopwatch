package com.neofastftl.nomsw.timing;

import com.neofastftl.nomsw.Config;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Measures world / multiplayer join load time from the moment loading begins
 * until the player can fully interact with the world.
 */
public final class WorldLoadTimer {

    private static final AtomicBoolean ACTIVE = new AtomicBoolean(false);
    private static final AtomicBoolean REPORTED = new AtomicBoolean(false);

    private static volatile Stopwatch watch;
    private static volatile String context = "";
    private static volatile String lastFormattedDuration;
    private static volatile int stableTicks;

    /** Consecutive client ticks that must look "interactive" before we finish. */
    private static final int REQUIRED_STABLE_TICKS = 2;

    private WorldLoadTimer() {
    }

    /**
     * Begins a new world-load measurement. If one is already running, it is
     * cancelled and replaced (e.g. reconnect / new world).
     */
    public static void begin(String loadContext) {
        if (!Config.measureWorldLoad) {
            return;
        }
        cancel();
        context = loadContext == null ? "unknown" : loadContext;
        watch = Stopwatch.start();
        ACTIVE.set(true);
        REPORTED.set(false);
        stableTicks = 0;
        TimingLog.note("WORLD", "Stopwatch started — " + context);
    }

    public static boolean isActive() {
        return ACTIVE.get() && !REPORTED.get();
    }

    public static void cancel() {
        Stopwatch w = watch;
        if (w != null && w.isRunning()) {
            w.cancel();
        }
        ACTIVE.set(false);
        REPORTED.set(false);
        stableTicks = 0;
        watch = null;
        context = "";
    }

    /**
     * Called every client tick while a load is active. When the world looks
     * fully interactive for several ticks in a row, the stopwatch is stopped
     * and results are logged.
     *
     * @param interactive whether the local game state is interactive right now
     * @return formatted duration when just completed, otherwise null
     */
    public static String tick(boolean interactive) {
        if (!isActive()) {
            return null;
        }
        if (!interactive) {
            stableTicks = 0;
            return null;
        }
        stableTicks++;
        if (stableTicks < REQUIRED_STABLE_TICKS) {
            return null;
        }
        return finish("player interactive");
    }

    private static String finish(String reason) {
        if (!REPORTED.compareAndSet(false, true)) {
            return null;
        }
        Stopwatch w = watch;
        if (w == null) {
            ACTIVE.set(false);
            return null;
        }
        w.stop();
        ACTIVE.set(false);
        lastFormattedDuration = w.formatElapsed();
        String detail = context + " | readyWhen=" + reason;
        TimingLog.record("WORLD", detail, w);
        return lastFormattedDuration;
    }

    public static Stopwatch getWatch() {
        return watch;
    }

    public static String getContext() {
        return context;
    }

    /** Last completed world-load duration, or null if none yet. */
    public static String getLastFormattedDuration() {
        return lastFormattedDuration;
    }
}
