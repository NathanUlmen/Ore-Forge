package ore.forge.Items;

public enum ItemRole {
    UPGRADER(1),
    DROPPER(2),
    FURNACE(3),
    CONVEYOR(4),
    TELEPORTER(5);

    public final int mask;

    ItemRole(int bitNum) {
        this.mask = (1 << bitNum);
    }

    public static int combineBits(ItemRole... roles) {
        int combined = 0;
        for (ItemRole role : roles) {
            combined |= role.mask;
        }
        return combined;
    }
}
