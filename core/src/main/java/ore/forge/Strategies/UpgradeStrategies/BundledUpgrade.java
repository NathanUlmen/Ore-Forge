package ore.forge.Strategies.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Ore;
import ore.forge.ReflectionLoader;

/**
 * @author Nathan Ulmen
 * Used to wrap/bundled any number of upgrades into one.
 */
public class BundledUpgrade implements UpgradeStrategy {
    private final UpgradeStrategy[] upgradeStrategies;

    public BundledUpgrade(UpgradeStrategy... upgradeStrategies) {
        this.upgradeStrategies = new UpgradeStrategy[upgradeStrategies.length];
        for (int i = 0; i < upgradeStrategies.length; i++) {
            this.upgradeStrategies[i] = upgradeStrategies[i];
        }
    }

    public BundledUpgrade(JsonValue jsonValue) {
//        this.upgradeStrategies = new UpgradeStrategy[jsonValue.size];
//        for (int i = 0; i < jsonValue.size; i++) {
//            this.upgradeStrategies[i] = createOrNull(jsonValue, "upgStrat" + String.valueOf(i + 1), "upgradeName");
//        }

        JsonValue strategyArray = jsonValue.get("upgrades");
        this.upgradeStrategies = new UpgradeStrategy[strategyArray.size];
        for (int i = 0; i < strategyArray.size; i++) {
            this.upgradeStrategies[i] = ReflectionLoader.createOrNull(strategyArray.get(i), "upgradeName");
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
        for (UpgradeStrategy upgStrat : upgradeStrategies) {
            if (upgStrat != null) {
                upgStrat.applyTo(ore);
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
