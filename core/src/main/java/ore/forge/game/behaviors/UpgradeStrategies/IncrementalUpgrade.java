package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;

/**
 * @author Nathan Ulmen *
 */
@SuppressWarnings("unused")
public class IncrementalUpgrade implements UpgradeStrategy {
    private int currentIndex;
    private final UpgradeStrategy[] strategies;

    public IncrementalUpgrade(IncrementalUpgrade incrementalUpgrade) {
        currentIndex = 0;
        strategies = new UpgradeStrategy[incrementalUpgrade.strategies.length];
        for (int i = 0; i < strategies.length; i++) {
            strategies[i] = strategies[currentIndex].cloneUpgradeStrategy();
        }
    }

    public IncrementalUpgrade(JsonValue jsonValue) {
        jsonValue = jsonValue.get("upgrades");
        currentIndex = 0;
        strategies = new UpgradeStrategy[jsonValue.size];
        for (int i = 0; i < strategies.length; i++) {
            strategies[i] = ReflectionLoader.load(jsonValue.get(i), "upgradeName");
            if (strategies[i] == null) {
                throw new IllegalArgumentException("Could not find UpgradeStrategy for " + jsonValue.get(i));
            }
        }
    }

    @Override
    public void applyTo(Ore ore) {
        strategies[currentIndex++].applyTo(ore);
        if (currentIndex > strategies.length) {
            currentIndex = 0;
        }
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return new IncrementalUpgrade(this);
    }
}
