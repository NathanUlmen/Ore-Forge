package ore.forge.game.expressions;

import ore.forge.game.Ore;

public enum UniqueMathFunctions {
    LN, LOG, SQRT, ABS; //SIN, COS, TAN;

    private interface FunctionBehavior {
        double calculate(Function function, Ore ore);
    }

    private final FunctionBehavior behavior;

    public double calculate(Function function, Ore ore) {
        return behavior.calculate(function, ore);
    }


    public static UniqueMathFunctions fromSymbol(String symbol) {
        if (symbol.contains("ln(") && symbol.charAt(1) == 'n') return UniqueMathFunctions.LN;
        if (symbol.contains("log(") && symbol.charAt(1) == 'o') return UniqueMathFunctions.LOG;
        if (symbol.contains("sqrt(")&& symbol.charAt(1) == 'q') return UniqueMathFunctions.SQRT;
        if (symbol.contains("abs(") && symbol.charAt(1) == 'b') return UniqueMathFunctions.ABS;
        throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }

    public String asSymbol() {
        return switch (this) {
            case LN -> "ln";
            case LOG -> "log";
            case SQRT -> "sqrt";
            case ABS -> "abs";
        };
    }

    public static boolean isMathFunction(String symbol) {
        return symbol.contains("ln(") || symbol.contains("log(") || symbol.contains("sqrt(") || symbol.contains("abs(");
    }

    UniqueMathFunctions() {
        behavior = switch (this) {
            case LN -> (function, ore) -> Math.log(function.calculate(ore));
            case LOG -> (function, ore) -> Math.log10(Math.abs(function.calculate(ore)));
            case SQRT -> (function, ore) -> Math.sqrt(function.calculate(ore));
            case ABS -> (function, ore) -> Math.abs(function.calculate(ore));
        };
    }

    public record SpecialFunction(Function function, UniqueMathFunctions specialFunction) implements NumericOperand {
        @Override
        public double calculate(Ore ore) {
            return specialFunction.calculate(function, ore);
        }

        public String toString() {
            return specialFunction.asSymbol() + "(" + function.toString() + ")";
        }
    }

}
