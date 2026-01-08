package ore.forge.Strategies.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Expressions.Operands.NumericOreProperties;
import ore.forge.Expressions.Operators.NumericOperator;
import ore.forge.Ore;

import java.util.function.Consumer;

/**
 * @author Nathan Ulmen
 * A Basic upgrade modifies an Ore property by applying the Modifier to it using a Numeric Operator.
 */
@SuppressWarnings("unused")
public class BasicUpgrade implements UpgradeStrategy {
    //More VTMS: effect, Duration.
    private double modifier;
    private final NumericOreProperties valueToModify;
    private final Consumer<Ore> upgradeFunction;
    private final NumericOperator numericOperator;


    public BasicUpgrade(double mod, NumericOperator numericOperatorType, NumericOreProperties valueToModify) {
        this.numericOperator = numericOperatorType;
        this.modifier = mod;
        this.valueToModify = valueToModify;
        upgradeFunction = configureUpgradeFunction();
    }

    public BasicUpgrade(JsonValue jsonValue) {
        //TODO: Introduce error handling for jsonValues.
        try {
            modifier = jsonValue.getDouble("modifier");
        } catch (IllegalArgumentException e) {
            modifier = 1;
        }
        valueToModify = NumericOreProperties.valueOf(jsonValue.getString("valueToModify"));
        numericOperator = NumericOperator.valueOf(jsonValue.getString("operator"));
        upgradeFunction = configureUpgradeFunction();
    }

    //Clone constructor
    private BasicUpgrade(BasicUpgrade upgrade) {
        this.modifier = upgrade.modifier;
        this.valueToModify = upgrade.valueToModify;
        this.numericOperator = upgrade.numericOperator;
        this.upgradeFunction = configureUpgradeFunction();
    }

    @Override
    public void applyTo(Ore ore) {
        upgradeFunction.accept(ore);//applies the function to the ore
    }

    //Determines/sets the behavior of the upgradeFunction.
    private Consumer<Ore> configureUpgradeFunction() {
        return switch (valueToModify) {
            case ORE_VALUE -> (Ore ore) -> ore.setOreValue(numericOperator.apply(ore.getOreValue(), modifier));
            case TEMPERATURE ->
                (Ore ore) -> ore.setTemp((float) Math.round(numericOperator.apply(ore.getOreTemp(), modifier)));
            case MULTIORE ->
                (Ore ore) -> ore.setMultiOre((int) Math.round(numericOperator.apply(ore.getOreTemp(), modifier)));
            case SPEED_SCALAR ->
                (Ore ore) -> ore.setSpeedScalar((float) numericOperator.apply(ore.getSpeedScalar(), modifier));
            case UPGRADE_COUNT, SPEED, RESET_COUNT ->
                throw new IllegalArgumentException(valueToModify + " is not a valid value to Modify.");
        };
    }

    public UpgradeStrategy cloneUpgradeStrategy() {
        return new BasicUpgrade(this);
    }

    public void setModifier(double newVal) {
        modifier = newVal;
    }

    public double getModifier() {
        return modifier;
    }

    public NumericOperator getOperator() {
        return numericOperator;
    }

    public NumericOreProperties getValueToModify() {
        return valueToModify;
    }

    public String toString() {
        return "[" + getClass().getSimpleName() + "]" +
            "\tValue To Modify: " + valueToModify +
            ", \tOperator: " + numericOperator +
            ", \tModifier: " + modifier;
    }

}
