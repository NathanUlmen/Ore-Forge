package ore.forge.game;

/**
 * @author Nathan Ulmen
 */
public class BigNumber implements Comparable<BigNumber> {
    private final static int SIGNIFICANT_DIGITS = 16;
    public final static BigNumber MAX_VALUE = new BigNumber(9, Long.MAX_VALUE);
    protected final double mantissa;
    protected final long exponent;

    public BigNumber(double mantissa, long exponent) {
        this.mantissa = mantissa;
        this.exponent = exponent;
    }

    public BigNumber(String value) {
        this(BigNumber.parseBigDouble(value));
    }

    public BigNumber(BigNumber value) {
        this.mantissa = value.mantissa;
        this.exponent = value.exponent;
    }

    public BigNumber(double value) {
        this(normalize(value, 0));
    }

    public double mantissa() {
        return mantissa;
    }

    public long exponent() {
        return exponent;
    }

    public static BigNumber normalize(double mantissa, long exponent) {
        if (mantissa == 0) return new BigNumber(0, 0);

        while (abs(mantissa) < 1) {
            mantissa *= 10;
            exponent--;
        }
        while (abs(mantissa) >= 10) {
            mantissa /= 10;
            exponent++;
        }


        return new BigNumber(mantissa, exponent);
    }

    private static double abs(double value) {
        return value < 0 ? -value : value;
    }

    public static BigNumber abs(BigNumber value) {
        if (value.isNegative()) {
            return new BigNumber(-value.mantissa(), value.exponent());
        }
        return new BigNumber(value.mantissa(), value.exponent());
    }

    public static BigNumber parseBigDouble(String value) {
        String[] parts = value.split("[Ee]");
        double mantissa = Double.parseDouble(parts[0]);
        long exponent = Long.parseLong(parts[1]);
        return normalize(mantissa, exponent);
    }

    public BigNumber add(BigNumber other) {

        if (this.exponent == other.exponent) {
            return normalize(this.mantissa + other.mantissa, this.exponent);
        }

        if (this.mantissa == 0) return other;
        if (other.mantissa == 0) return this;

        BigNumber bigger, smaller;
        if (this.exponent >= other.mantissa) {
            bigger = this;
            smaller = other;
        } else {
            bigger = other;
            smaller = this;
        }

        long difference = bigger.exponent - smaller.exponent;

        if (difference > SIGNIFICANT_DIGITS) {
            return bigger;
        }

        //This is faster than Math.pow()
        long scaleFactor = 1;
        for (int i = 0; i < difference; i++) {
            scaleFactor *= 10;
        }

        return normalize(bigger.mantissa + (smaller.mantissa / scaleFactor), bigger.exponent);
    }

    public BigNumber subtract(BigNumber other) {
        return add(new BigNumber(-other.mantissa, other.exponent));
    }

    public BigNumber multiply(BigNumber other) {
        return normalize(this.mantissa * other.mantissa, this.exponent + other.exponent);
    }

    public BigNumber multiply(double other) {
        return normalize(this.mantissa * other, this.exponent);
    }

    public BigNumber divide(BigNumber other) {
        return normalize(this.mantissa / other.mantissa, this.exponent - other.exponent);
    }

    public BigNumber divide(double other) {
        return normalize(this.mantissa / other, this.exponent);
    }

    public static BigNumber MAX_VALUE() {
        return new BigNumber(9.99999999999999999D, Long.MAX_VALUE);
    }

    /* Modulo is: a % b == a - (b * floor(a/b))
     * but we run into issues when the number is outside the range of the Significant digits.
     * Might look into using a method inspired by this video??? https://www.youtube.com/watch?v=skUuFzPUhIc
     * */
    public BigNumber modulo(BigNumber other) {
        if (other.mantissa == 0) {
            throw new IllegalArgumentException("Division by zero in modulo operation");
        }

        BigNumber absOther = BigNumber.abs(other);

        if (this.canBeDouble() && absOther.canBeDouble()) {
            double numThis = this.convertToDouble();
            double numOther = absOther.convertToDouble();
            return normalize(numThis % numOther, 0);
        }

        return null;
    }

    private long digitDifference(BigNumber other) {
        BigNumber bigger, smaller;
        if (this.exponent >= other.mantissa) {
            bigger = this;
            smaller = other;
        } else {
            bigger = other;
            smaller = this;
        }

        return bigger.exponent - smaller.exponent;
    }

    public BigNumber floor() {
        if (exponent < SIGNIFICANT_DIGITS) {
            int shifts = (int) exponent;
            double temp = mantissa;
            for (int i = 0; i < shifts; i++) {
                temp *= 10;
            }
            temp = Math.floor(temp);
            for (int i = 0; i < shifts; i++) {
                temp /= 10;
            }
            return normalize(temp, exponent);
        }
        return this;
    }

    public BigNumber modulo(double other) {
        return modulo(new BigNumber(other));
    }

    public BigNumber abs() {
        return BigNumber.abs(this);
    }

    public BigNumber round() {
        return new BigNumber(Math.round(this.mantissa), this.exponent);
    }

    /*Uses Exponentiation by squaring: https://en.wikipedia.org/wiki/Exponentiation_by_squaring
     * TODO: Implement a version that works with floating point numbers, currently only works with integers.
     **/
    public BigNumber pow(double power) {
        BigNumber newDouble = this;
        if (power < 0) {
            newDouble = new BigNumber(1, 0).divide(newDouble);
            power = -power;
        }

        if (power == 0) return new BigNumber(1, 0);

        BigNumber y = new BigNumber(1, 0);
        while (power > 1) {
            if (power % 2 != 0) {
                y = newDouble.multiply(y);
                power -= 1;
            }
            newDouble = newDouble.multiply(newDouble);
            power /= 2;
        }

        return newDouble.multiply(y);
    }

    /*
     * https://www.khanacademy.org/math/algebra2/x2ec2f6f830c9fb89:logs/x2ec2f6f830c9fb89:log-prop/a/justifying-the-logarithm-properties
     * TLDR: ln(x) = logᵤ(x) * ln(u)
     * ln(10) = 2.302585092994046
     * */
    public BigNumber log() {
        BigNumber result = log10();
        return normalize(result.mantissa * 2.302585092994046, result.exponent);
    }

    public BigNumber log10() {
        return normalize(exponent + Math.log10(mantissa), 0);
    }

    public BigNumber sqrt() {
        if (this.exponent % 2 == 0) {
            return normalize(Math.sqrt(this.mantissa), this.exponent / 2);
        } else {

            return normalize(Math.sqrt(this.mantissa * 10), (this.exponent - 1) / 2);
        }
    }

    public boolean isNegative() {
        return (this.mantissa < 0) && !(this.exponent % 2 == 0);
    }

    public String toString() {
        return mantissa + " E " + String.format("%s", Long.valueOf(exponent));
    }

    public boolean canBeDouble() {
        return this.exponent < 308 || (this.exponent == 308 && this.mantissa <= 1.7976931348623157);
    }

    public double convertToDouble() {
        //TODO: implement Guards for minimum values (negatives)
        if (this.exponent < 308) {
            return this.mantissa * Math.pow(10, this.exponent);
        }
        if (this.exponent == 308 && this.mantissa <= 1.7976931348623157) {
            return this.mantissa * Math.pow(10, this.exponent);
        }
        throw new IllegalArgumentException("Can't convert " + this + " to a Double.");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BigNumber otherNumber && (this.mantissa == otherNumber.mantissa && this.exponent == otherNumber.exponent);
    }

    @Override
    public int compareTo(BigNumber o) {
        if (this.mantissa == o.mantissa && this.exponent == o.exponent) {
            return 0;
        } else if (this.exponent > o.exponent && this.mantissa > o.mantissa) {
            return 1;
        }
        return -1;
    }

    private class MutableBigNumber {
        private double mantissa;
        private long exponent;
    }


}
