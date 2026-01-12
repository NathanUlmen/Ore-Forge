package ore.forge;

public enum CollisionRules {
    ORE(0),
    ORE_PROCESSOR(1),
    WORLD_GEOMETRY(2);

    public final int mask;

    CollisionRules(int bitNum) {
        this.mask = (1 << bitNum);
    }

    public int getBit() {
        return mask;
    }

    public static int combineBits(CollisionRules... rules) {
        if (rules == null) return -1;
        int combined = 0;
        for (CollisionRules rule : rules) {
            combined |= rule.getBit();
        }
        return combined;
    }

}
