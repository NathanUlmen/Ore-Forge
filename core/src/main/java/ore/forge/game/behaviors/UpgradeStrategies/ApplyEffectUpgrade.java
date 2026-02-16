package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;
import ore.forge.game.behaviors.OreEffects.OreEffect;


/**
 * @author Nathan Ulmen
 * Applies an OreEffect to an ore.
 */
@SuppressWarnings("unused")
public class ApplyEffectUpgrade implements UpgradeStrategy {
    private final OreEffect effect;

    public ApplyEffectUpgrade(OreEffect strategy) {
        effect = strategy;
    }

    public ApplyEffectUpgrade(JsonValue jsonValue) {
        effect = ReflectionLoader.load(jsonValue.get("effectToApply"), "effectName");
    }

    @Override
    public void applyTo(Ore ore) {
        ore.applyEffect(effect);
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "]\t" + effect.toString();
    }
}
