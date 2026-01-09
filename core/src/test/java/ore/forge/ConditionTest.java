package ore.forge;

import ore.forge.Expressions.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConditionTest {
    private final Ore ore = new Ore();

    @BeforeEach
    void setUp() {
        ore.deepReset();
    }

    @Test
    void testNumberComparison() {
        var testCase = Condition.compile("0==0");
        assertTrue(testCase.evaluate(null));
    }

    @Test
    void testStringComparison() {
        ore.setOreName("test");
        var testCase = Condition.compile("\"test\" == ORE_NAME");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testFunctionComparison() {
        ore.setOreValue(20);
        var testCase = Condition.compile("{(ORE_VALUE*4)} > 79");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testLogicalAND() {
        ore.setOreValue(5);
        var testCase = Condition.compile("ORE_VALUE > 4 AND {(ORE_VALUE%5)} == 0");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testLogicalNOT() {
        ore.setOreValue(5);
        var testCase = Condition.compile("ORE_VALUE != 0");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testLogicalOR() {
        ore.setTemperature(50);
        ore.setOreValue(20);
        var testCase = Condition.compile("TEMPERATURE > 50 OR ORE_VALUE < 50");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testXOR() {
        ore.setTemperature(20);
        ore.setOreValue(30);
        var testCase = Condition.compile("TEMPERATURE != 0 XOR ORE_VALUE < 20");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testComplexCondition2() {
        ore.setOreName("test");
        ore.setOreValue(15);
        var testCase = Condition.compile("ORE_NAME == \"test\" AND ! ORE_VALUE > 20");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testCollectionBasedCondition2() {
        ore.getUpgradeTag(new UpgradeTag("Foolish Tag", "1234", 5, false));
        var testCase = Condition.compile("UPGRADE_TAGS.CONTAINS(1234)");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testNumericRetriever() {
        var testCase = Condition.compile("UPGRADE_TAGS.GET_COUNT(1234) <= 0");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testOreTemperature() {
        ore.setTemperature(-30);
        var testCase = Condition.compile("TEMPERATURE < 0");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testBiconditionalIsTrue() {
        ore.setOreName("foo");
        ore.setOreValue(20);
        var testCase = Condition.compile("ORE_NAME == \"foo\" <-> ORE_VALUE >= 20");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testBiconditionalIsFalse() {
        ore.setOreName("bar");
        ore.setOreValue(20);
        //Left is True. Right is False. Result should be False.
        var testCase = Condition.compile("ORE_NAME == \"bar\" <-> ORE_VALUE > 100");
        assertFalse(testCase.evaluate(ore));
    }

    @Test
    void testTwoFalseBiconditional() {
        ore.setOreName("foo");
        ore.setOreValue(44);
        //Left is False. Right is False. Result should be True.
        var testCase = Condition.compile("ORE_NAME == \"bar\" <-> ORE_VALUE > 100");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testComplexString() {
        ore.setOreName("The Best Ore Ever");
        ore.setOreValue(101);
        var testCase = Condition.compile("ORE_NAME == \"The Best Ore Ever\"");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testStringWithColon() {
        ore.setOreName("Star Wars: The Empire Strikes Back");
        var testCase = Condition.compile("ORE_NAME == \"Star Wars: The Empire Strikes Back\"");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testStringWithApostrophes() {
        ore.setOreName("Turtles' Ore");
        var testCase = Condition.compile("ORE_NAME == \"Turtles' Ore\"");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testStringWithDigits() {
        ore.setOreName("Uranium-238");
        var testCase = Condition.compile("ORE_NAME == \"Uranium-238\"");
        assertTrue(testCase.evaluate(ore));
    }

    @Test
    void testParen() {
//        ore.setOreName("foo");
//        ore.setOreValue(3);
//        var testCase = Condition.compile("!(ORE_NAME == \"foo\" && ORE_VALUE == 2)");
//        assertFalse(testCase.evaluate(ore));
    }

}
