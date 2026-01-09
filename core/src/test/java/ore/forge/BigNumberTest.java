package ore.forge;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nathan Ulmen
 */

public class BigNumberTest {
    Stopwatch stopwatch = new Stopwatch(TimeUnit.MICROSECONDS);

    @Test
    void testNormalizationWithBigMantissa() {
        var result = BigNumber.normalize(300, 20);
        assertEquals(3, result.mantissa());
        assertEquals(22, result.exponent());
    }

    @Test
    void testNormalizationWithSmallMantissa() {
        var result = BigNumber.normalize(0.003, 20);
        assertEquals(3, result.mantissa());
        assertEquals(17, result.exponent());
    }

    @Test
    void testMultiplication() {
        BigNumber value1 = new BigNumber(3, 10);
        BigNumber value2 = new BigNumber(4, 5);
        var result = value1.multiply(value2);
        assertEquals(1.2, result.mantissa());
        assertEquals(16, result.exponent());
    }

    @Test
    void testDivision() {
        var result = new BigNumber(6, 50).divide(new BigNumber(3, 5));
        assertEquals(2, result.mantissa());
        assertEquals(45, result.exponent());
    }

    @Test
    void testAdditionWhenBiggerThanSigDigits() {
        var result = new BigNumber(6, 50).add(new BigNumber(3, 5));
        assertEquals(6, result.mantissa());
        assertEquals(50, result.exponent());
    }

    @Test
    void testAddition() {
        var result = new BigNumber(4, 4).add(new BigNumber(3, 3));
        assertEquals(4.3, result.mantissa());
        assertEquals(4, result.exponent());
    }

    @Test
    void randomAddition() {
        var value1 = new BigNumber(3, 10);
        var value2 = new BigNumber(2, 5);

        var result = value1.add(value2);
        assertEquals(3.00002, result.mantissa());
        assertEquals(10, result.exponent());
    }

    @Test
    void testSubtraction() {
        var value1 = new BigNumber(4, 10);
        var value2 = new BigNumber(2, 5);
        var result = value1.subtract(value2);

        assertEquals(3.99998, result.mantissa());
        assertEquals(10, result.exponent());
    }

    @Test
    void testPOW() {
        var value = new BigNumber(3, 1);
        var result = value.pow(3);
        value = new BigNumber(2, 0);
        assertEquals(2.7, result.mantissa());
        assertEquals(4, result.exponent());

    }

    @Test
    void testNegativePow() {
        var value = new BigNumber(-1.2, 3);
        var result = value.pow(3);
        assertEquals(-1.728, result.mantissa());
        assertEquals(9, result.exponent());
        System.out.println(value.pow(1.1));
    }


    @Test
    void testLog10() {
        var result = new BigNumber(1, 21).log10();
        assertEquals(2.1, result.mantissa());
        assertEquals(1, result.exponent());
    }

    @Test
    void testNaturalLog() {
        var result = new BigNumber(1, 41).log();
        assertEquals(9.440598881275587, result.mantissa());
        assertEquals(1, result.exponent());
    }

    @Test
    void testFloor() {
        double maxDifference = 0.0001f;
        assertEquals(54236, new BigNumber("5.423697E4").floor().convertToDouble(), maxDifference);
    }

    @Test
    void testModulo() {
        var result = new BigNumber(1, 1).modulo(3);
        assertEquals(10 % 3, result.convertToDouble());
    }

    @Test
    void testModuloBig() {
        BigNumber left = new BigNumber(BigNumber.MAX_VALUE);
        BigNumber right = new BigNumber(3);
        assertEquals(Double.MAX_VALUE % 3, left.modulo(right).convertToDouble());
    }

    @Test
    void testSQRT() {
        var result = new BigNumber(3, 10).sqrt();
        assertEquals(Math.sqrt(3), result.mantissa());
        assertEquals(5, result.exponent());
    }

    @Test
    void testOddSQRT() {
        var result = new BigNumber(3, 11).sqrt();
        assertEquals(Math.sqrt(30), result.mantissa());
        assertEquals(5, result.exponent());
    }

    @Test
    public void testDifferentHugeNumberModulo() {
        // Create another pair of large BigDecimal numbers
        String num1 = "3.6e410";
        String num2 = "6e400";
        BigDecimal bigDecimalNum1 = new BigDecimal(num1);
        BigDecimal bigDecimalNum2 = new BigDecimal(num2);

        BigDecimal bigDecimalResult = bigDecimalNum1.remainder(bigDecimalNum2, new MathContext(1, RoundingMode.HALF_UP));

        // Create corresponding BigNumber objects
        BigNumber bigNumberNum1 = new BigNumber(num1);
        BigNumber bigNumberNum2 = new BigNumber(num2);

        // Perform modulo operation with BigNumber
        BigNumber bigNumberResult = bigNumberNum1.modulo(bigNumberNum2);

        // Compare the results of BigDecimal and BigNumber modulo
        System.out.println(bigDecimalResult);
        System.out.println(bigNumberResult);
        BigDecimal bigNumberMantissa = new BigDecimal(bigNumberResult.toString());
        BigDecimal bigNumberMantissaTrimmed = bigNumberMantissa.setScale(16, RoundingMode.HALF_UP);

        assertEquals(bigDecimalResult.setScale(16, RoundingMode.HALF_UP), bigNumberMantissaTrimmed,
            "BigDecimal result and BigNumber result do not match!");
    }

    @Test
    void foo() {
        System.out.println(Double.MAX_VALUE % 3);
        System.out.println(Double.MAX_VALUE - (30000 * Math.floor(Double.MAX_VALUE / 30000)));
        System.out.println(Math.floor(Double.MAX_VALUE / 3));
        System.out.println(3 * Math.floor(Double.MAX_VALUE / 3));
        System.out.println();
    }


}
