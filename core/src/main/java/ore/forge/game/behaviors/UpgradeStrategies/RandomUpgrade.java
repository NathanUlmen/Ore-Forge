package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("unused")
public class RandomUpgrade implements UpgradeStrategy {
    private final Random random = new Random(123);
    private final HashMap<Float, ArrayList<UpgradeStrategy>> chanceTable;
    private final ArrayList<Float> keys;


    public RandomUpgrade(JsonValue jsonValue) {
        chanceTable = new HashMap<>();
        JsonValue upgrades = jsonValue.get("upgrades");
        for (JsonValue upgrade : upgrades) {
            addUpgrade(upgrade.getFloat("chance"), ReflectionLoader.load(upgrade, "upgradeName"));
        }

        keys = new ArrayList<>(chanceTable.keySet());
        Collections.sort(keys);

    }

    public RandomUpgrade(HashMap<Float, ArrayList<UpgradeStrategy>> chanceTable) {
        this.chanceTable = new HashMap<>();
        for (Float key : chanceTable.keySet()) {
            for (UpgradeStrategy strategy : chanceTable.get(key)) {
                addUpgrade(key, strategy);
            }
        }

        keys = new ArrayList<>(chanceTable.keySet());
        Collections.sort(keys);
    }

    public RandomUpgrade(RandomUpgrade upgradeToClone) {
        chanceTable = new HashMap<>();
        for (Float key : upgradeToClone.chanceTable.keySet()) {
            for (UpgradeStrategy strategy : upgradeToClone.chanceTable.get(key)) {
                addUpgrade(key, strategy.cloneUpgradeStrategy());
            }
        }

        keys = new ArrayList<>(chanceTable.keySet());
        Collections.sort(keys);
    }

    @Override
    public void applyTo(Ore ore) {
        float roll = generateRoll();
        if (roll >= keys.getLast()) {
            getStrategyFromBucket(chanceTable.get(keys.getLast())).applyTo(ore);
        } else {
            for (float key : keys) {
                if (roll <= key) {
                    getStrategyFromBucket(chanceTable.get(key)).applyTo(ore);
                    break;
                }
            }
        }
    }

    private UpgradeStrategy getStrategyFromBucket(ArrayList<UpgradeStrategy> bucket) {
        return bucket.get(random.nextInt(bucket.size()));
    }

    private float generateRoll() {
        return BigDecimal.valueOf(random.nextFloat() * 100).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return new RandomUpgrade(this);
    }

    @Override
    public String toString() {
        return "[RandomUpgrade]";
    }

    private void addUpgrade(Float chance, UpgradeStrategy upgradeStrategy) {
        if (!chanceTable.containsKey(chance)) {
            ArrayList<UpgradeStrategy> strategies = new ArrayList<>();
            strategies.add(upgradeStrategy);
            chanceTable.put(chance, strategies);
        } else {
            assert !chanceTable.get(chance).contains(upgradeStrategy);
            chanceTable.get(chance).add(upgradeStrategy);
        }
    }
}
