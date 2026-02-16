package ore.forge.game.expressions;

import ore.forge.game.Ore;

import java.util.function.Function;

public interface NumericOperand extends Function<Ore, Double> {
    double calculate(Ore ore);

    default Double apply(Ore ore) {
        return calculate(ore);
    }
}
