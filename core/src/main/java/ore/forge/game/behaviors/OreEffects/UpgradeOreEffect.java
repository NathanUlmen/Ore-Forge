package ore.forge.game.behaviors.OreEffects;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.game.behaviors.UpgradeStrategies.UpgradeStrategy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//@author Nathan Ulmen
@SuppressWarnings("unused")
public class UpgradeOreEffect implements OreEffect {
    private final float interval;
    private float duration;
    private float currentTime;
    private final UpgradeStrategy strategy;

    public UpgradeOreEffect(float interval, float effectDuration, UpgradeStrategy strategyToApply) {
        this.duration = effectDuration;
        this.interval = interval;
        strategy = strategyToApply;
    }

    private UpgradeOreEffect(UpgradeOreEffect clone) {
        this.interval = clone.interval;
        this.duration = clone.duration;
        currentTime = 0f;
        strategy = clone.strategy.cloneUpgradeStrategy();
    }

    public UpgradeOreEffect(JsonValue jsonValue) {
        try {
            Class<?> aClass = Class.forName(jsonValue.get("upgrade").getString("upgradeName"));
            Constructor<?> constructor = aClass.getConstructor(JsonValue.class);
            this.strategy = (UpgradeStrategy) constructor.newInstance(jsonValue.get("upgrade"));
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InvocationTargetException
                 | InstantiationException
                 | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.duration = jsonValue.getFloat("duration");
        this.interval = jsonValue.getFloat("interval");
        this.currentTime = 0;
    }

    @Override
    public void activate(float deltaT, Ore ore) {
        duration -= deltaT;
        if (duration <= 0) {
            ore.removeEffect(this);
        }
        currentTime += deltaT;
        while (currentTime >= interval) {
            strategy.applyTo(ore);
            currentTime -= interval;
        }
    }

    @Override
    public OreEffect cloneOreEffect() {
        return new UpgradeOreEffect(this);
    }

    public String toString() {
        return "\n" + getClass().getSimpleName() + "\tInterval: " + interval +
            "\tDuration: " + duration + "\nUpgradeStrategy: " + strategy.toString();
    }
}
