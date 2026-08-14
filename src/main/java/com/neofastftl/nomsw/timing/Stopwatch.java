package com.neofastftl.nomsw.timing;

import java.util.concurrent.TimeUnit;

/**
 * Simple high-resolution stopwatch. Supports an absolute wall-clock start
 * (e.g. JVM start) as well as a nanoTime-based start for mid-session timings.
 */
public final class Stopwatch {

    private long startNano;
    private long startEpochMs;
    private long endNano;
    private long endEpochMs;
    private boolean running;
    private boolean finished;

    private Stopwatch(long startNano, long startEpochMs) {
        this.startNano = startNano;
        this.startEpochMs = startEpochMs;
        this.running = true;
        this.finished = false;
    }

    /** Starts a stopwatch from "now". */
    public static Stopwatch start() {
        return new Stopwatch(System.nanoTime(), System.currentTimeMillis());
    }

    /**
     * Starts a stopwatch whose elapsed time is measured from a known epoch-ms
     * moment (e.g. {@code ManagementFactory.getRuntimeMXBean().getStartTime()}).
     * Nano elapsed is derived so duration math stays consistent.
     */
    public static Stopwatch startFromEpochMs(long epochMs) {
        long nowEpoch = System.currentTimeMillis();
        long nowNano = System.nanoTime();
        long elapsedMs = Math.max(0L, nowEpoch - epochMs);
        long startNano = nowNano - TimeUnit.MILLISECONDS.toNanos(elapsedMs);
        return new Stopwatch(startNano, epochMs);
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isFinished() {
        return finished;
    }

    public long getStartEpochMs() {
        return startEpochMs;
    }

    /** Elapsed nanos so far (if still running) or until stop (if finished). */
    public long elapsedNanos() {
        long end = finished ? endNano : System.nanoTime();
        return Math.max(0L, end - startNano);
    }

    public long elapsedMillis() {
        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos());
    }

    public double elapsedSeconds() {
        return elapsedNanos() / 1_000_000_000.0;
    }

    public long stop() {
        if (!running) {
            return elapsedNanos();
        }
        endNano = System.nanoTime();
        endEpochMs = System.currentTimeMillis();
        running = false;
        finished = true;
        return elapsedNanos();
    }

    public void cancel() {
        running = false;
        finished = false;
        endNano = 0L;
        endEpochMs = 0L;
    }

    public long getEndEpochMs() {
        return endEpochMs;
    }

    /** Human-readable duration, e.g. {@code 12.345s} or {@code 1m 05.230s}. */
    public static String formatDuration(long nanos) {
        long totalMs = TimeUnit.NANOSECONDS.toMillis(nanos);
        long minutes = totalMs / 60_000L;
        long seconds = (totalMs % 60_000L) / 1000L;
        long millis = totalMs % 1000L;
        if (minutes > 0) {
            return String.format("%dm %02d.%03ds", minutes, seconds, millis);
        }
        return String.format("%d.%03ds", seconds, millis);
    }

    public String formatElapsed() {
        return formatDuration(elapsedNanos());
    }
}
