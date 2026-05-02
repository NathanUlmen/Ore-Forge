package ore.forge.engine;

public enum VertexAttribute {
    POSITION(0, Float.BYTES * 3),
    NORMAL(0, Float.BYTES * 3),
    TANGENT(0, Float.BYTES * 4),
    TEXCOORD_0(0, Float.BYTES * 4),
    JOINTS_0(0, Float.BYTES * 2),
    WEIGHTS_0(0, Float.BYTES * 4);

    VertexAttribute(int index, int sizeInBytes) {
        this.index = index;
        this.sizeInBytes = sizeInBytes;
    }

    private int index;
    private int sizeInBytes;

    public int index() {
        return index;
    }

    public int sizeInBytes() {
        return sizeInBytes;
    }

}
