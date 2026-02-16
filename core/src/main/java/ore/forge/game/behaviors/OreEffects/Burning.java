package ore.forge.game.behaviors.OreEffects;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;

@SuppressWarnings("unused")
public class Burning implements OreEffect {
    private float currentDuration;
    private final float tempIncrease, duration;

    public Burning(float duration, float tempIncrease) {
        this.currentDuration = duration;
        this.duration = currentDuration;
        this.tempIncrease = tempIncrease;
    }

    public Burning(JsonValue jsonValue) {
        this.currentDuration = jsonValue.getFloat("duration");
        this.duration = currentDuration;
        this.tempIncrease = jsonValue.getFloat("tempIncrease");
    }

    //Clone constructor
    private Burning(Burning burning) {
        this.currentDuration = burning.duration;
        this.duration = burning.duration;
        this.tempIncrease = burning.tempIncrease;
    }

    @Override
    public void activate(float deltaTime, Ore ore) {
        currentDuration -= deltaTime;
        if (currentDuration <= 0) {
            ore.setIsDoomed(true);
            ore.removeEffect(this);
        } else {
            ore.setTemperature(ore.getOreTemp() + tempIncrease * deltaTime);
        }
    }

    @Override
    public OreEffect cloneOreEffect() {
        return new Burning(this);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "\tDuration: " + currentDuration + "\tTemp Increase: " + tempIncrease;
    }
}
