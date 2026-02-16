package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;

/**
 * @author Nathan Ulmen
 * Used to wrap/bundled any number of upgrades into one.
 */
@SuppressWarnings("unused")
public class BundledUpgrade implements UpgradeStrategy {
    private final UpgradeStrategy[] upgradeStrategies;

    public BundledUpgrade(UpgradeStrategy... upgradeStrategies) {
        this.upgradeStrategies = new UpgradeStrategy[upgradeStrategies.length];
        System.arraycopy(upgradeStrategies, 0, this.upgradeStrategies, 0, upgradeStrategies.length);
    }

    public BundledUpgrade(JsonValue jsonValue) {
//        this.upgradeStrategies = new UpgradeStrategy[jsonValue.size];
//        for (int i = 0; i < jsonValue.size; i++) {
//            this.upgradeStrategies[i] = createOrNull(jsonValue, "upgStrat" + String.valueOf(i + 1), "upgradeName");
//        }

        JsonValue strategyArray = jsonValue.get("upgrades");
        this.upgradeStrategies = new UpgradeStrategy[strategyArray.size];
        for (int i = 0; i < strategyArray.size; i++) {
            this.upgradeStrategies[i] = ReflectionLoader.load(strategyArray.get(i), "upgradeName");
        }

    }

    //Clone constructor
    private BundledUpgrade(BundledUpgrade bundledUpgradeClone) {
        this.upgradeStrategies = new UpgradeStrategy[bundledUpgradeClone.upgradeStrategies.length];
        for (int i = 0; i < bundledUpgradeClone.upgradeStrategies.length; i++) {
            if (bundledUpgradeClone.upgradeStrategies[i] != null) {
                this.upgradeStrategies[i] = bundledUpgradeClone.upgradeStrategies[i].cloneUpgradeStrategy();
            }
        }
    }

    @Override
    public void applyTo(Ore ore) {
        for (UpgradeStrategy upgradeStrat : upgradeStrategies) {
            if (upgradeStrat != null) {
                upgradeStrat.applyTo(ore);
            }
        }
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return new BundledUpgrade(this);
    }


    public String toString() {
        StringBuilder s = new StringBuilder();
        for (UpgradeStrategy upgStrat : upgradeStrategies) {
            if (upgStrat != null) {
                s.append("\n").append(upgStrat);
            }
        }
        return s.toString();
    }

}
