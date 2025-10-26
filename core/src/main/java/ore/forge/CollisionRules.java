package ore.forge;

public enum CollisionRules {
    ORE(0),
    ORE_PROCESSOR(1),
    WORLD_GEOMETRY(2);

    private final int bit;

    CollisionRules(int bitNum) {
        this.bit = (1 << bitNum);
    }

    public int getBit() {
        return bit;
    }

    public static int combineBits(CollisionRules... rules) {
        int combined = 0;
        for (CollisionRules rule : rules) {
            combined |= rule.getBit();
        }
        return combined;
    }

}
