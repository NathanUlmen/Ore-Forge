package ore.forge.engine;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * @author Nathan Ulmen
 * <p>
 * Mesh data before being uploaded to GPU.
 * VBO format will be done in the following order
 * 1. Position
 * 2. Normal
 * 3. Tangent
 * 4. Texture
 * 4. Color
 * 5. Joint
 * 6. Bone Weight
 */
public final class MeshData implements AssetData {
    private final ByteBuffer vbo;
    private final IntBuffer ebo;

    public MeshData(ByteBuffer vbo, IntBuffer ebo) {
        this.vbo = vbo;
        this.ebo = ebo;
    }

    public ByteBuffer vbo() {
        return vbo;
    }

    public IntBuffer ebo() {
        return ebo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MeshData other) {
            return vbo.equals(other.vbo) && ebo.equals(other.ebo);
        }
        return false;
    }

}

