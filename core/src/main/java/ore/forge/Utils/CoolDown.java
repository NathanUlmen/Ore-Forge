package ore.forge.Utils;

/**
 * @author Nathan Ulmen
 * <p>
 * Represents a simple cooldown timer that tracks elapsed time and determines
 * if a specified time interval (the "cooldown period") has been reached. *
 * Each time the cooldown period completes, an internal activation count is incremented.
 * This can be used for things like status effects which activate or do something on an interval.
 * </p>
 */
public class CoolDown {
    private int activationCount;
    private final float finishTime;
    private float currentTime;

    /**
     * @param finishTime The time in seconds required for the cooldown to complete.
     */
    public CoolDown(float finishTime) {
        this.finishTime = finishTime;
        currentTime = 0f;
    }

    /**
     * Updates the internal timer with the time that has passed since the last frame or update.
     *
     * <p>
     * This method should be called once per frame, passing in the frame's delta time.
     * If the elapsed time reaches or exceeds the finish time, the activation count is incremented
     * and the internal timer is reset to account for any overflow.
     * </p>
     *
     * @param deltaTime The time in seconds that has passed since the last update.
     * @return {@code true} if the cooldown period has elapsed and the cooldown has "activated", {@code false} otherwise.
     */
    public int update(float deltaTime) {
        currentTime += deltaTime;
        int count = 0;
        while (currentTime >= finishTime) {
            activationCount++;
            count++;
            currentTime -= finishTime;
        }
        return count;
    }

    /**
     * Resets teh current elapsed time to zero, restarting the cooldown.
     */
    public void resetCurrentTime() {
        currentTime = 0f;
    }

    /**
     * @return The current elapsed time in seconds.
     */
    public float getCurrentTime() {
        return currentTime;
    }

    /**
     * @return The cooldown duration in seconds.
     */
    public float getFinishTime() {
        return finishTime;
    }

    /**
     * @return The number of times the cooldown has been "activated" / reached its finish time.
     */
    public int getActivationCount() {
        return activationCount;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "current time: " + currentTime + " reset time: " + finishTime + " activation count: " + activationCount;
    }

}
