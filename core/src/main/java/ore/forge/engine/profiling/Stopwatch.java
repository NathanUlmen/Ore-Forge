package ore.forge.engine.profiling;


import java.util.concurrent.TimeUnit;

/**
 * @author Nathan Ulmen
 * A simple utility class intended to help profile code.
 *
 */
public final class Stopwatch {

    public enum State {NEW, RUNNING, STOPPED}

    private final TimeUnit unit;

    private State state = State.NEW;

    private long startNanos = 0L;       // when current run started
    private long stopNanos = 0L;        // when last stopped
    private long accumulatedNanos = 0L; // time from previous runs

    private long lastLapNanos = 0L;

    public Stopwatch(TimeUnit unit) {
        if (unit == null) throw new IllegalArgumentException("TimeUnit must not be null");
        this.unit = unit;
    }

    /**
     * Starts if NEW or STOPPED; no-op if already RUNNING.
     */
    public Stopwatch start() {
        if (state == State.RUNNING) return this;

        long now = System.nanoTime();
        startNanos = now;
        lastLapNanos = now;
        state = State.RUNNING;
        return this;
    }

    /**
     * Stops if RUNNING; no-op otherwise.
     */
    public void stop() {
        if (state != State.RUNNING) return;

        stopNanos = System.nanoTime();
        accumulatedNanos += (stopNanos - startNanos);
        state = State.STOPPED;
    }

    /**
     * Clears all timing and returns to NEW state.
     */
    public Stopwatch reset() {
        state = State.NEW;
        startNanos = 0L;
        stopNanos = 0L;
        accumulatedNanos = 0L;
        lastLapNanos = 0L;
        return this;
    }

    /**
     * Equivalent to reset().start().
     */
    public void restart() {
        reset().start();
    }

    /**
     * Total elapsed time in the configured unit, regardless of running/stopped/new.
     */
    public long elapsed() {
        return unit.convert(elapsedNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Total elapsed time in nanos.
     */
    public long elapsedNanos() {
        return switch (state) {
            case NEW -> 0L;
            case STOPPED -> accumulatedNanos;
            case RUNNING -> accumulatedNanos + (System.nanoTime() - startNanos);
        };
    }

    /**
     * Returns time since last lap (or since start), and resets the lap point.
     * If not running, returns 0.
     */
    public long lap() {
        if (state != State.RUNNING) return 0L;

        long now = System.nanoTime();
        long delta = now - lastLapNanos;
        lastLapNanos = now;
        return unit.convert(delta, TimeUnit.NANOSECONDS);
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }

    public State state() {
        return state;
    }

    public TimeUnit unit() {
        return unit;
    }

    @Override
    public String toString() {
        return "Stopwatch{" +
            "state=" + state +
            ", elapsed=" + elapsed() + " " + unit +
            '}';
    }
}

