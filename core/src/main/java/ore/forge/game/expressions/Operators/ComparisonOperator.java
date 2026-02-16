package ore.forge.game.expressions.Operators;


import java.util.function.Function;

public enum ComparisonOperator {
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL_TO,
    LESS_THAN_EQUAL_TO,
    EQUAL_TO,
    NOT_EQUAL_TO;

    public final boolean evaluate(double left, double right) {
        return comparator.evaluate(left, right);
    }

    public <E extends Comparable<E>> boolean compare(E left, E right) {
        return comparisonOperator.apply(left.compareTo(right));
    }

    @FunctionalInterface
    private interface DoubleBooleanEvaluator {
        boolean evaluate(double x, double y);
    }

    private interface Comparison<E extends Comparable<E>> {
        boolean evaluate(E left, E right);
    }

    private final DoubleBooleanEvaluator comparator;
    private final Function<Integer, Boolean> comparisonOperator;

    public static ComparisonOperator fromSymbol(String symbol) {
        return switch (symbol) {
            case ">" -> GREATER_THAN;
            case "<" -> LESS_THAN;
            case ">=" -> GREATER_THAN_EQUAL_TO;
            case "<=" -> LESS_THAN_EQUAL_TO;
            case "==" -> EQUAL_TO;
            case "!=" -> NOT_EQUAL_TO;
            default -> throw new IllegalArgumentException("Unexpected value: " + symbol);
        };
    }

    public static boolean isOperator(String symbol) {
        return switch (symbol) {
            case ">", "<", ">=", "<=", "==", "!=" -> true;
            default -> false;
        };
    }

    public String asSymbol() {
        return switch (this) {
            case GREATER_THAN -> ">";
            case LESS_THAN -> "<";
            case GREATER_THAN_EQUAL_TO -> ">=";
            case LESS_THAN_EQUAL_TO -> "<=";
            case EQUAL_TO -> "==";
            case NOT_EQUAL_TO -> "!=";
        };
    }

    ComparisonOperator() {
        comparator = switch (this) {
            case GREATER_THAN -> (x, y) -> x > y;
            case LESS_THAN -> (x, y) -> x < y;
            case GREATER_THAN_EQUAL_TO -> (x, y) -> x >= y;
            case LESS_THAN_EQUAL_TO -> (x, y) -> x <= y;
            case EQUAL_TO -> (x, y) -> x == y;
            case NOT_EQUAL_TO -> (x, y) -> x != y;
        };

        comparisonOperator = switch (this) {
            case GREATER_THAN -> (result) -> result > 0;
            case LESS_THAN -> (result) -> result < 0;
            case GREATER_THAN_EQUAL_TO -> (result) -> result >= 0;
            case LESS_THAN_EQUAL_TO -> (result) -> result <= 0;
            case EQUAL_TO -> (result) -> result == 0;
            case NOT_EQUAL_TO -> (result) -> result != 0;
        };
    }
}
