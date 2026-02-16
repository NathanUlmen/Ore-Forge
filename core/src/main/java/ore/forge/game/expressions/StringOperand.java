package ore.forge.game.expressions;

import ore.forge.game.Ore;

import java.util.function.Function;

public interface StringOperand extends Function<Ore, String> {
    String asString(Ore ore);

    default String apply(Ore ore) {
        return asString(ore);
    }

}
