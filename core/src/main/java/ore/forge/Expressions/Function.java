package ore.forge.Expressions;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Expressions.Operands.MethodBasedOperand;
import ore.forge.Expressions.Operands.NumericOreProperties;
import ore.forge.Expressions.Operands.ValueOfInfluence;
import ore.forge.Expressions.Operators.NumericOperator;
import ore.forge.Ore;

import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nathan Ulmen
 * A pair of parenthesis encapsulate a function.
 * A function is composed of a left operand, right operand, and an operator.
 * An operand can be a KeyValue, Fixed number(double, int, float, etc.), or another Function.
 * KeyValues are Enums. Each enum will call its associated method to get its value.
 * EX: if the enum is ORE_VALUE then calling ORE_VALUE.getAssociatedValue(ore) will return the value of the ore.
 * This class will parse an equation from a String and return a Function.
 */
public class Function implements NumericOperand {
    private final static Pattern pattern = Pattern.compile(
        "(([A-Z_]+\\.)([A-Z_]+)\\(([^)]+)\\))|(log\\(|sqrt\\(|ln\\(|abs\\()((?:[^)(]|\\((?:[^)(]|\\((?:[^)(]|\\([^)(]*\\))*\\))*\\))*)|([a-zA-Z_]+)|(-?\\d*\\.?\\d+(?:[eE][-+]?\\d+)?)|\\(|\\)|([+\\-*/^=%])");
    private final NumericOperand leftNumericOperand, rightNumericOperand;
    private final NumericOperator numericOperator;

    private Function(NumericOperand leftNumericOperand, NumericOperand rightNumericOperand, NumericOperator numericOperator) {
        this.leftNumericOperand = leftNumericOperand;
        this.rightNumericOperand = rightNumericOperand;
        this.numericOperator = numericOperator;
    }

    //Takes a Json Value, extracts the function string from it, then creates a function object based on the extracted string
    /**
     * */
    public static Function compile(JsonValue jsonValue) {
        String equation = jsonValue.getString("upgradeFunction");
        return compile(equation);
    }

    /** Takes a string that represents a mathematical function, tokenizes it, and creates a mathematical function from it.
     * @param equation - string representation of a mathematical function.
     * @return parsed function of the inputted string.
     */
    public static Function compile(String equation) {
        equation = equation.replaceAll("(\\d+)([-+])(\\d+)", "$1 $2 $3"); //get rid of spaces in Function string.
        Matcher matcher = pattern.matcher(equation);
        return parseFromTokens(matcher);
    }

    /**
     * Uses the Shunting Yard Algorithm: <a href="https://en.wikipedia.org/wiki/Shunting_yard_algorithm">...</a>
     * to parse the mathematical functions from a string.
     */
    private static Function parseFromTokens(Matcher matcher) {
        Stack<NumericOperand> operandStack = new Stack<>();
        Stack<NumericOperator> operatorStack = new Stack<>();
        while (matcher.find()) {
            String token = matcher.group();
            if (isNumeric(token)) {
                operandStack.push(new Constant(Double.parseDouble(token)));
            } else if (NumericOreProperties.isProperty(token)) {
                operandStack.push(NumericOreProperties.valueOf(token));
            } else if (ValueOfInfluence.isValue(token)) {
                operandStack.push(ValueOfInfluence.valueOf(token));
            } else if (token.equals("(")) {
                //check for implied multiplication by looking at available operands.
                if (!operandStack.isEmpty() && operandStack.peek() instanceof NumericOperand && operatorStack.isEmpty()) {
                    operatorStack.push(NumericOperator.MULTIPLY);
                }
                operatorStack.push(null); //push to simulate the parenthesis
            } else if (token.equals(")")) {
                while (operatorStack.peek() != null) { // "collapse" function till we hit the opening parenthesis.
                    operandStack.push(createFunction(operandStack, operatorStack));
                }
                operatorStack.pop();//remove the null
            } else if (UniqueMathFunctions.isMathFunction(token)) { //Special Functions like ln
                UniqueMathFunctions function = UniqueMathFunctions.fromSymbol(token);
                operandStack.push(new UniqueMathFunctions.SpecialFunction(compile(matcher.group(6)), function)); //group 2 is the contents inside special function.
                matcher.find(); //Get rid of the Trailing )
                assert Objects.equals(matcher.group(), ")");
            } else if (NumericOperator.isOperator(token)) {
                NumericOperator operator = NumericOperator.fromSymbol(token);
                while (!operatorStack.isEmpty() && operatorStack.peek() != null &&
                    ((operator.getAssociativity() == NumericOperator.Associativity.LEFT && operator.getPrecedence() <= operatorStack.peek().getPrecedence()) ||
                        (operator.getAssociativity() == NumericOperator.Associativity.RIGHT && operator.getPrecedence() < operatorStack.peek().getPrecedence()))) {
                    operandStack.push(createFunction(operandStack, operatorStack));
                }
                operatorStack.push(operator);
            } else if (token.contains("(") && token.contains(")") && matcher.group(2).charAt(matcher.group(2).length() - 1) == '.') {//Method verification.
                var argumentSource = matcher.group(2);
                argumentSource = argumentSource.substring(0, argumentSource.length() - 1);
                if (MethodBasedOperand.isCollection(argumentSource)) { //Verify that collection is valid.
                    var method = matcher.group(3);
                    if (MethodBasedOperand.methodIsValid(method)) { //Verify that method is valid
                        if (method.equals("GET_COUNT")) { //Determine return type of method(number vs boolean)
                            operandStack.push(new Condition.NumericMethodOperand(matcher.group(4), MethodBasedOperand.valueOf(argumentSource)));
                        } else {
                            throw new IllegalArgumentException("Cant use CONTAINS in function.");
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }

        while (!operatorStack.isEmpty()) {
            operandStack.push(createFunction(operandStack, operatorStack));
        }

        if (!(operandStack.peek() instanceof Function)) {
            return new Function(new Constant(0), operandStack.pop(), NumericOperator.ASSIGNMENT);
        }

        return (Function) operandStack.pop();
    }

    private static NumericOperand createFunction(Stack<NumericOperand> operandStack, Stack<NumericOperator> operatorStack) {
        NumericOperand right = operandStack.pop();
        NumericOperand left = operandStack.pop();
        NumericOperator functionOperator = operatorStack.pop();
        return new Function(left, right, functionOperator);
    }

    @Override
    public double calculate(Ore ore) {
        return numericOperator.apply(leftNumericOperand.calculate(ore), rightNumericOperand.calculate(ore));
    }

    /**
     *
     */
    public static boolean isNumeric(String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (leftNumericOperand + " " + numericOperator.asSymbol() + " " + rightNumericOperand);
    }

    /*
     * Record class to keep track of primitive doubles
     */
    public record Constant(double value) implements NumericOperand {
        @Override
        public double calculate(Ore ore) {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

}
