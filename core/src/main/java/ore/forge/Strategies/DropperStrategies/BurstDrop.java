package ore.forge.Strategies.DropperStrategies;

import ore.forge.Utils.CoolDown;
import com.badlogic.gdx.utils.JsonValue;

@SuppressWarnings("unused")
public class BurstDrop implements DropStrategy {
    private final float orePerMinute;
    private final float burstCount;
    private float dropAccumulator;
    private float burstAccumulator;
    private int currentOreInBurst;
    private boolean isDropping;

    //burstCooldown in the CD between bursts.
    //intervalBetween is the time between each ore in the burst
    private final CoolDown burstCooldown, intervalBetween;


    public BurstDrop(JsonValue jsonValue) {
        orePerMinute = jsonValue.getFloat("orePerMinute");
        burstCount = jsonValue.getInt("burstCount");
        var burstPerSec = (orePerMinute / burstCount) / 60f;
        var cooldownInterval = 1 / burstPerSec;
        var orePerSec = orePerMinute / 60f;
        var intervalPerOre = 1 / orePerSec;

        cooldownInterval /= 2;
        burstCooldown = new CoolDown(cooldownInterval);
        currentOreInBurst = 0;


        intervalPerOre /= 2;
        intervalBetween = new CoolDown(intervalPerOre);
        isDropping = false;

        dropAccumulator = 0;
        burstAccumulator = 0;
    }

    // oreCooldown, burstCount, ore per minute.
    public BurstDrop(float orePerMinute, float burstCount) {
        this.orePerMinute = orePerMinute;
        this.burstCount = burstCount;
        var burstPerSec = (orePerMinute / burstCount) / 60f;
        var cooldownInterval = 1 / burstPerSec;
        var orePerSec = orePerMinute / 60f;
        var intervalPerOre = 1 / orePerSec;

        cooldownInterval /= 2;
        burstCooldown = new CoolDown(cooldownInterval);
        currentOreInBurst = 0;

        intervalPerOre /= 2f;
        intervalBetween = new CoolDown(intervalPerOre);
        isDropping = false;
        dropAccumulator = 0;
        burstAccumulator = 0;
    }

    public BurstDrop(BurstDrop toBeCloned) {
        this(toBeCloned.orePerMinute, toBeCloned.burstCount);
    }


//    @Override
//    public int drop(float delta) {
//        if (!isDropping) {
//            if (burstCooldown.update(delta) > 0) {
//                isDropping = true;
//                currentOreInBurst = 0;
//            }
//        } else {
//            int activationCount = intervalBetween.update(delta);

    /// /            if (intervalBetween.update(delta) && currentOreInBurst < burstCount) {
//            if (activationCount > 0 && currentOreInBurst < burstCount) {
//                currentOreInBurst += activationCount;
//                if (currentOreInBurst == burstCount) {
//                    isDropping = false;
//                }
//                return currentOreInBurst;
//            }
//        }
//        return 0;
//    }
    @Override
    public int drop(float delta) {
        int produced = 0;

        while (delta > 0f) {
            // ===========================
            // IDLE / COOLDOWN STATE
            // ===========================
            if (!isDropping) {
                float timeToBurstStart = burstCooldown.getFinishTime() - burstAccumulator;

                if (delta < timeToBurstStart) {
                    burstAccumulator += delta;
                    return produced;
                }

                // We reach burst start this frame
                delta -= timeToBurstStart;

                // Start burst
                burstAccumulator = 0f;
                isDropping = true;
                currentOreInBurst = 0;
                dropAccumulator = 0f;

                // IMPORTANT: continue loop with remaining delta,
                // which now belongs to the burst.
                continue;
            }

            // ===========================
            // BURST DROPPING STATE
            // ===========================
            if (currentOreInBurst >= burstCount) {
                // Burst is done; switch back to cooldown.
                isDropping = false;
                // cooldown starts now; keep looping so leftover delta counts toward cooldown
                continue;
            }

            float timeToNextDrop = intervalBetween.getFinishTime() - dropAccumulator;

            if (delta < timeToNextDrop) {
                dropAccumulator += delta;
                return produced;
            }

            // We hit a drop boundary this frame
            delta -= timeToNextDrop;

            // Emit exactly one drop
            dropAccumulator = 0f;
            currentOreInBurst++;
            produced++;

            // If that was the last ore in burst, loop continues and
            // leftover delta will start cooldown immediately.
            if (currentOreInBurst >= burstCount) {
                isDropping = false;
                burstAccumulator = 0f; // start cooldown timing fresh *after* burst completion
            }
        }
        return produced;
    }


}
