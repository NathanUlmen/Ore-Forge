package ore.forge.game.expressions.Operators;

import ore.forge.game.expressions.BooleanOperator;

public enum LogicalOperator implements BooleanOperator {
    NOT,
    XOR,
    AND,
    OR,
    BICONDITIONAL;


    public boolean evaluate(boolean left, boolean right) {
        return operator.applyTo(left, right);
    }


    public static LogicalOperator fromString(String string) {
        string = string.toUpperCase();
        return switch (string) {
            case "NOT", "!" -> LogicalOperator.NOT;
            case "XOR", "^" -> LogicalOperator.XOR;
            case "AND", "&&" -> LogicalOperator.AND;
            case "OR", "||" -> LogicalOperator.OR;
            case "<->" -> LogicalOperator.BICONDITIONAL;
            default -> throw new IllegalStateException("Unexpected value: " + string);
        };
    }

    public static boolean isOperator(String string) {
        return switch (string) {
            case "NOT", "!", "XOR", "^", "AND", "&&", "OR", "||", "->", "<->" -> true;
            default -> false;
        };
    }

    public String toString() {
        return switch (this) {
            case NOT -> "!";
            case XOR -> "^";
            case AND -> "&&";
            case OR -> "||";
            case BICONDITIONAL -> "<->";
        };
    }


    private final BooleanOperator operator;
    private final int precedence;

    LogicalOperator() {
        operator = switch (this) {
            case NOT -> (left, right) -> !right;
            case XOR -> (left, right) -> left ^ right;
            case AND -> (left, right) -> left && right;
            case OR -> (left, right) -> left || right;
            case BICONDITIONAL -> (left, right) -> (left && right) || (!left && !right);
        };
        precedence = switch (this) {
            case NOT -> 5;
            case AND -> 4;
            case XOR -> 3;
            case OR -> 2;
            case BICONDITIONAL -> 1;
        };
    }

    public int getPrecedence() {
        return this.precedence;
    }


    @Override
    public boolean applyTo(boolean left, boolean right) {
        return false;
    }

}
