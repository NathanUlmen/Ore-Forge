package ore.forge.engine;

import com.badlogic.gdx.graphics.VertexAttributes;


public enum VertexAttribute {
    POSITION(0, 3, Float.BYTES, VertexAttributes.Usage.Position),
    NORMAL(0, 3, Float.BYTES, VertexAttributes.Usage.Normal),
    TANGENT(0, 4, Float.BYTES, VertexAttributes.Usage.Tangent),

    COLOR_0(0, 4, Float.BYTES, VertexAttributes.Usage.ColorUnpacked),

    TEXCOORD_0(0, 2, Float.BYTES, VertexAttributes.Usage.TextureCoordinates),
    TEXCOORD_1(1, 2, Float.BYTES, VertexAttributes.Usage.TextureCoordinates),

    JOINTS_0(0, 4, Float.BYTES, VertexAttributes.Usage.Generic),
    WEIGHTS_0(0, 4, Float.BYTES, VertexAttributes.Usage.BoneWeight),

    JOINTS_1(1, 4, Float.BYTES, VertexAttributes.Usage.Generic),
    WEIGHTS_1(1, 4, Float.BYTES, VertexAttributes.Usage.BoneWeight);

    private final int index;
    private final int componentCount;
    private final int componentSizeInBytes;
    private final int usage;

    VertexAttribute( int index, int componentCount, int componentSizeInBytes, int usage) {
        this.index = index;
        this.componentCount = componentCount;
        this.componentSizeInBytes = componentSizeInBytes;
        this.usage = usage;
    }

    public int index() {
        return index;
    }

    public int slot() {
        return ordinal();
    }

    public int sizeInBytes() {
        return componentCount * componentSizeInBytes;
    }

    public String alias() {
        return switch (this) {
            case POSITION -> "a_position";
            case NORMAL -> "a_normal";
            case TANGENT -> "a_tangent";
            case COLOR_0 -> "a_color";
            case TEXCOORD_0 -> "a_texCoord0";
            case TEXCOORD_1 -> "a_texCoord1";
            case JOINTS_0 -> "a_joints0";
            case WEIGHTS_0 -> "a_weights0";
            case JOINTS_1 -> "a_joints1";
            case WEIGHTS_1 -> "a_weights1";
        };
    }

    public com.badlogic.gdx.graphics.VertexAttribute toGdxAttribute() {
        return new com.badlogic.gdx.graphics.VertexAttribute(
            usage,
            componentCount,
            alias()
        );
    }
}
