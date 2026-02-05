package ore.forge.Render;

public enum RenderFlags {
    TRANSPARENT (1 << 0),
    DEPTH_WRITE (1 << 1),
    DEPTH_TEST  (1 << 2),
    EMISSIVE    (1 << 3),
    UNLIT       (1 << 4),
    OUTLINE     (1 << 5),
    GLOW_PASS   (1 << 6);

    public final int bit;

    RenderFlags(int bit) {
        this.bit = bit;
    }

    public static int mask(RenderFlags... flags) {
        int m = 0;
        for (RenderFlags f : flags) m |= f.bit;
        return m;
    }

}
