package ore.forge;

import ore.forge.game.Ore;
import ore.forge.game.expressions.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionTest {
    private final static OreRealm oreRealm = OreRealm.getSingleton();
    private final Ore ore = new Ore();

    @BeforeEach
    void setUp() {
        oreRealm.depopulate();
        oreRealm.populate();
        oreRealm.resetAllOre();
    }

    @Test
    void testAddition() {
        Function simpleAddition = Function.compile(("2+2"));
        assertEquals(4, simpleAddition.calculate(ore));
    }

    @Test
    void testSubtraction() {
        Function simpleSubtraction = Function.compile("2-2");
        assertEquals(0, simpleSubtraction.calculate(ore));
    }

    @Test
    void testMultiplication() {
        Function simpleMultiplication = Function.compile("(3*2)");
        assertEquals(6, simpleMultiplication.calculate(ore));
    }

    @Test
    void testDivision() {
        Function simpleDivision = Function.compile("6/2");
        assertEquals(3, simpleDivision.calculate(ore));
    }

    @Test
    void testModulo() {
        Function simpleModulo = Function.compile("(4%2)");
        assertEquals(0, simpleModulo.calculate(ore));
    }

    @Test
    void testExponent() {
        Function simpleExponent = Function.compile("(3^2)");
        assertEquals(9, simpleExponent.calculate(ore));
    }

    @Test
    void testAssignment() {
        Function simpleAssignment = Function.compile("(0=3)");
        assertEquals(3, simpleAssignment.calculate(ore));
    }

    @Test
    void testOreValueProperty() {
        ore.setOreValue(20);
        Function simpleValueProperty = Function.compile("(ORE_VALUE*2)");
        assertEquals(40, simpleValueProperty.calculate(ore));
    }

    @Test
    void testTemperatureProperty() {
        ore.setTemperature(15);
        Function simpleTemperatureProperty = Function.compile("(TEMPERATURE*2)");
        assertEquals(30, simpleTemperatureProperty.calculate(ore));
    }

    @Test
    void testUpgradeCountProperty() {
        ore.setUpgradeCount(5);
        Function simpleUpgradeCountProperty = Function.compile("(UPGRADE_COUNT+2)");
        assertEquals(7, simpleUpgradeCountProperty.calculate(ore));
    }

    @Test
    void testSpeedScalarProperty() {
        ore.setSpeedScalar(3);
        var speedScalarProperty = Function.compile("(SPEED_SCALAR+5)");
        assertEquals(8, speedScalarProperty.calculate(ore));
    }

    @Test
    void testSubtractionAndNegative() {
        var testCase = Function.compile("(7--3)");
        assertEquals(10, testCase.calculate(ore));
    }

    @Test
    void testSubtractionAndNegativeWithParenthesis() {
        var testCase = Function.compile("((5+10)--5)");
        assertEquals(20, testCase.calculate(ore));
    }

    @Test
    void testScientificNotation() {
        var testCase = Function.compile("(3E10+1)");
        assertEquals(Double.parseDouble("3E10") + 1, testCase.calculate(ore));
    }

    @Test
    void testScientificNotationWithDecimal() {
        var testCase = Function.compile("(2.252E8-42E3)");
        assertEquals(Double.parseDouble("2.252E8") - Double.parseDouble("42E3"), testCase.calculate(ore));
    }

    @Test
    void testNewParse() {
        var testCase = Function.compile("(36*3+2)/2");
        assertEquals(55, testCase.calculate(ore));
    }

    @Test
    void testParenthesis() {
        ore.setOreValue(10);
        var testCase = Function.compile("(ORE_VALUE % 3 + 1) * 4");
        assertEquals(8, testCase.calculate(ore));
    }

    @Test
    void testParenthesisWithParenthesis() {
        var testCase = Function.compile("(10 % 3 + 1)*4 + 36 - 2 + (360--9)");
        assertEquals(411, testCase.calculate(null));
    }

    @Test
    void testExplicitMultiply() {
        var testCase = Function.compile("(5+2) * (3+2)");
        assertEquals(35, testCase.calculate(ore));
    }

    @Test
    void testSpecialFunction() {
        var testCase = Function.compile("ln(30)/2");
        assertEquals(Math.log(30) / 2, testCase.calculate(ore));
    }

    @Test
    void testNestedSpecialFunction() {
        var testCase = Function.compile("ln(log(sqrt(30)))");
        assertEquals(Math.log(Math.log10(Math.sqrt(30))), testCase.calculate(ore));
    }

    @Test
    void testAbsoluteValue() {
        var testCase = Function.compile("abs(90)");
        assertEquals(Math.abs(-90), testCase.calculate(null));
    }

    @Test
    void testComplexFunction() {
        ore.setTemperature(100);
        var testCase = Function.compile("(abs((TEMPERATURE * log(TEMPERATURE)) / 30)) ^ 1.03 + 1");
        assertEquals(Math.pow(Math.abs(ore.getOreTemp() * Math.log10(ore.getOreTemp()) / 30), 1.03) + 1, testCase.calculate(ore));
    }

    @Test
    void testAverageOreValue() {
        int value = 10;
        while (!oreRealm.getStackOfOre().isEmpty()) {
            oreRealm.giveOre().setOreValue(value);
        }
        var testCase = Function.compile("AVG_ORE_VALUE");
        double result = 0;
        for (Ore ore : oreRealm.getActiveOre()) {
            result += ore.getOreValue();
        }
        result /= oreRealm.getActiveOre().size();
        assertEquals(result, testCase.calculate(null));
    }

    @Test
    void testMedianOreValue() {
//        int[] array = new int[]{12, 3, 5, 7, 4, 19, 26};
        int[] array = new int[]{2, 3, 1, 4, 5, 0};
        for (int num : array) {
            oreRealm.giveOre().setOreValue(num);
        }
        var testCase = Function.compile("MEDIAN_ORE_VALUE");
        assertEquals(2.5, testCase.calculate(null));
    }

    @Test
    void testNumericMethod() {
        for (int i = 0; i < 5; i++) {
            oreRealm.giveOre().applyBaseStats(1, 5, 1, "NumericMethodTestOre", "321", null);
        }
        var testCase = Function.compile("ACTIVE_ORE.GET_COUNT(321)");
        assertEquals(5, testCase.calculate(null));
    }

    @Test
    void testSpecialFunctionsAndNumericMethod() {
        for (int i = 0; i < 5; i++) {
            oreRealm.giveOre().applyBaseStats(1, 5, 1, "Line208", "321", null);
        }
        var testCase = Function.compile("ln(ACTIVE_ORE.GET_COUNT(321) * log(ACTIVE_ORE.GET_COUNT(321))) ^ 1.03 + 1");
        assertEquals(Math.pow(Math.log(5 * Math.log10(5)), 1.03) + 1, testCase.calculate(null));
    }

    @Test
    void testImpliedMultiply() {
        var testCase = Function.compile("7(4+3)");
        assertEquals(49, testCase.calculate(null));
    }

    @Test
    void testImpliedMultiply2() {
        ore.setOreValue(10);
        ore.setTemperature(1);
        ore.setMultiOre(5);
        var testCase = Function.compile("ORE_VALUE(TEMPERATURE + MULTIORE)");
        assertEquals(60, testCase.calculate(ore));
    }

    @Test
    void testImpliedMultiplySpecialFunction() {
        ore.setOreValue(10);
        var testCase = Function.compile("ORE_VALUE(sqrt(30))");
        assertEquals(10 * Math.sqrt(30), testCase.calculate(ore));
    }

    @Test
    void testImpliedMultiplyParen() {
        var testCase = Function.compile("(5)(3)");
        assertEquals(15, testCase.calculate(null));
    }

}
