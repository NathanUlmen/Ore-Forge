package ore.forge;


import java.util.concurrent.TimeUnit;

public class Stopwatch {
    private enum State {RUNNING, STOPPED, RESET}

    private long startTime, endTime;
    private State state;
    private final TimeUnit timeUnit;

    public Stopwatch(TimeUnit timeUnit) {
        state = State.RESET;
        this.timeUnit = timeUnit;
    }

    public long getElapsedTime() {
        if (state == State.RESET) {
            throw new IllegalStateException("Stopwatch has not been started.");
        } else if (state == State.RUNNING) {
            throw new IllegalStateException("Stopwatch is currently running.");
        }
        return this.timeUnit.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    }

    public long getTimeStamp() {
        if (state == State.RESET) {
            throw new IllegalStateException("Stopwatch is not running");
        }
        return this.timeUnit.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }

    public void start() {
        if (state == State.RUNNING) {
            throw new IllegalStateException("Stopwatch is already running");
        }
        state = State.RUNNING;
        startTime = System.nanoTime();
    }

    public void stop() {
        if (state != State.RUNNING) {
            throw new IllegalStateException("Stopwatch is already stopped.");
        }
        state = State.STOPPED;
        endTime = System.nanoTime();
    }

    public void reset() {
        startTime = endTime = 0;
        state = State.RESET;
    }

    public void restart() {
        state = State.RUNNING;
        startTime = System.nanoTime();
    }

    public void print() {
        System.out.println(this);
    }

    public boolean isRunning() {
        return state == State.RUNNING;
    }
    public String toString() {
        return switch (state) {
            case RUNNING -> "Current Elapsed time: " + getTimeStamp() + " " + this.timeUnit;
            case STOPPED -> "Elapsed Time: " + getElapsedTime() + " " + this.timeUnit;
            case RESET -> "Stopwatch has not been started.";
        };
    }
}
