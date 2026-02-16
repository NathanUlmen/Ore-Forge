package ore.forge.game.behaviors.OreEffects;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;

@SuppressWarnings("unused")
public class FrostBite implements OreEffect {
    private float currentDuration;
    private final float tempDecrease, duration;


    public FrostBite(float duration, float tempDecrease) {
        this.currentDuration = duration;
        this.duration = duration;
        this.tempDecrease = tempDecrease;

    }

    public FrostBite(JsonValue jsonValue) {
        this.currentDuration = jsonValue.getFloat("duration");
        this.duration = currentDuration;
        this.tempDecrease = jsonValue.getFloat("tempDecrease");
    }

    //Clone constructor
    private FrostBite(FrostBite frostBite) {
        this.currentDuration = frostBite.duration;
        this.duration = frostBite.duration;
        this.tempDecrease = frostBite.tempDecrease;
    }

    @Override
    public void activate(float deltaT, Ore ore) {
        currentDuration -= deltaT;
        if (currentDuration <= 0) {
//            ore.setSpeedScalar(1);
            ore.removeEffect(this);
        } else {
//            ore.setSpeedScalar(.5f);
            ore.setTemperature(ore.getOreTemp() + tempDecrease * deltaT);
        }
    }

    @Override
    public OreEffect cloneOreEffect() {
        return new FrostBite(this);
    }

    @Override
    public String toString() {
        return "FrostBite{" +
            "duration=" + currentDuration +
            "tempDecrease=" + tempDecrease +
            '}';
    }
}
