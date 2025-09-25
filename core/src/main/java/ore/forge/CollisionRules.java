package ore.forge;

public enum CollisionRules {
    ORE_PROCESSOR(0),
    ORE(1);

    private final short bit;

    CollisionRules(int bitNum) {
        this.bit = (short) (1 << bitNum);
    }

    public short getBit() {
        return bit;
    }

    public static short combineBits(CollisionRules ...rules) {
        short combined = 0;
        for (CollisionRules rule : rules) {
            combined |= rule.getBit();
        }
        return combined;
    }

}
