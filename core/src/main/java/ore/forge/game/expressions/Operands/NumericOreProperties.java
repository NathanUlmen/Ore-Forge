package ore.forge.game.expressions.Operands;

import ore.forge.game.expressions.NumericOperand;
import ore.forge.game.Ore;


public enum NumericOreProperties implements NumericOperand {
    ORE_VALUE,
    TEMPERATURE,
    MULTIORE,
//    SPEED,
//    SPEED_SCALAR,
    UPGRADE_COUNT,
    RESET_COUNT;

    public final double getAssociatedValue(Ore ore) {
        return supplier.getValue(ore);
    }

    @Override
    public double calculate(Ore ore) {
        return supplier.getValue(ore);
    }

    private interface ValueGetter {
        double getValue(Ore ore);
    }

    public static boolean isProperty(String property) {
        return switch (property) {
            case "ORE_VALUE", "TEMPERATURE", "MULTIORE", "SPEED", "SPEED_SCALAR", "UPGRADE_COUNT" -> true;
            default -> false;
        };
    }

    private final ValueGetter supplier;

    NumericOreProperties() {
        supplier = switch (this) {
            case ORE_VALUE -> (Ore::getOreValue);
            case TEMPERATURE -> (Ore::getOreTemp);
            case MULTIORE -> (Ore::getMultiOre);
//            case SPEED -> (Ore::getMoveSpeed);
//            case SPEED_SCALAR -> (Ore::getSpeedScalar);
            case UPGRADE_COUNT -> (Ore::getUpgradeCount);
            case RESET_COUNT -> (Ore::getResetCount);
        };
    }
}
