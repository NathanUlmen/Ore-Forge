package ore.forge.game.behaviors.OreEffects;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.expressions.Operands.ValueOfInfluence;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;
import ore.forge.game.behaviors.UpgradeStrategies.UpgradeStrategy;

//@author Nathan Ulmen
//TODO: Invulnerability could have its implementation changed to be an ObserverEffect???
//On state change/notify evaluate a condition then do something depending on the result of this evaluation.
@SuppressWarnings("unused")
public class ObserverOreEffect implements OreEffect {
    //On upgrade(temp change, value change, multi-ore change, Speed Change,
    private final float duration;
    private final int charges;
    private final UpgradeStrategy upgradeStrategy;


    public ObserverOreEffect(float duration, int charges, UpgradeStrategy strategy) {
        this.duration = duration;
        this.charges = charges;
        this.upgradeStrategy = strategy;
    }

    public ObserverOreEffect(JsonValue jsonValue) {
        this.duration = jsonValue.getFloat("duration");
        this.charges = jsonValue.getInt("charges");
//        upgradeStrategy = ReflectionLoader.create(jsonValue, "upgrade", "upgradeName");
        upgradeStrategy = ReflectionLoader.load(jsonValue.get("upgrade"), "upgradeName");
    }

    //Clone Constructor
    private ObserverOreEffect(ObserverOreEffect clone) {
        this.duration = clone.duration;
        this.charges = clone.charges;
        this.upgradeStrategy = clone.upgradeStrategy.cloneUpgradeStrategy();
    }

    @Override
    public void activate(float deltaT, Ore ore) {
        if (upgradeStrategy != null) {
            upgradeStrategy.applyTo(ore);
        }
    }

    public ValueOfInfluence getObservedField() {
        return null;
    }

    @Override
    public OreEffect cloneOreEffect() {
        return new ObserverOreEffect(this);
    }

}
