package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.expressions.Function;
import ore.forge.game.expressions.Operands.NumericOreProperties;
import ore.forge.game.expressions.Operators.NumericOperator;
import ore.forge.game.Ore;

/**
 * @author Nathan Ulmen
 * An Influenced Upgrade dynamically changes/adapts the value of its Modifier based on the returned result of its Function.
 * A maximum and minimum value can be set to ensure the modifier stays within a specified range.
 */
@SuppressWarnings("unused")
public class InfluencedUpgrade implements UpgradeStrategy {
    private final BasicUpgrade baseUpgrade;
    private final Function upgradeFunction;
    private final double minModifier, maxModifier;

    public InfluencedUpgrade(Function upgradeFunction, BasicUpgrade upgrade, double minModifier, double maxModifier) {
        this.baseUpgrade = upgrade;
        this.upgradeFunction = upgradeFunction;
        this.minModifier = minModifier;
        this.maxModifier = maxModifier;
    }

    public InfluencedUpgrade(JsonValue jsonValue) {
        upgradeFunction = Function.compile(jsonValue.getString("upgradeFunction"));

        NumericOperator operator = NumericOperator.valueOf(jsonValue.getString("operator"));
        NumericOreProperties valueToModify = NumericOreProperties.valueOf(jsonValue.getString("valueToModify"));
        baseUpgrade = new BasicUpgrade(0, operator, valueToModify);

        //If field doesn't exist or is null that means we need to set it to the "default" .
        double temp;
        try {
            temp = jsonValue.getDouble("minModifier");
        } catch (IllegalArgumentException | IllegalStateException e) {
            temp = Double.MIN_VALUE;
        }
        minModifier = temp;
        try {
            temp = jsonValue.getDouble("maxModifier");
        } catch (IllegalArgumentException | IllegalStateException e) {
            temp = Double.MAX_VALUE;
        }
        maxModifier = temp;
    }

    private InfluencedUpgrade(InfluencedUpgrade influencedUpgradeClone) {
        this.baseUpgrade = influencedUpgradeClone.baseUpgrade;//Don't need to clone
        this.upgradeFunction = influencedUpgradeClone.upgradeFunction;
        this.minModifier = influencedUpgradeClone.minModifier;
        this.maxModifier = influencedUpgradeClone.maxModifier;
    }

    @Override
    public void applyTo(Ore ore) {
        double originalModifier = baseUpgrade.getModifier();
        double newModifier = upgradeFunction.calculate(ore);
//        Gdx.app.log("INFLUENCED UPGRADE", "New Modifier" + String.valueOf(newModifier));

        if (newModifier > maxModifier) {
            baseUpgrade.setModifier(maxModifier);
        } else if (newModifier < minModifier) {
            baseUpgrade.setModifier(minModifier);
        } else {
            baseUpgrade.setModifier(newModifier);
        }

        baseUpgrade.applyTo(ore);
        baseUpgrade.setModifier(originalModifier);
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "]" +
            "\tOperator Type: " + baseUpgrade.getOperator() +
            ", Value To Modify: " + baseUpgrade.getValueToModify() +
            ", Upgrade Function: " + upgradeFunction +
            ", Minimum Modifier: " + minModifier +
            ", Max Modifier: " + maxModifier;
    }

}
