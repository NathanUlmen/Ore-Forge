package ore.forge.engine.definitions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
public class MeshData {
    private final ByteBuffer vbo;
    private final IntBuffer ebo;
    private AssetRecord assetRecord;

    public MeshData(ByteBuffer vbo, IntBuffer ebo) {
        this.vbo = vbo;
        this.ebo = ebo;
    }

    public void setAssetRecord(AssetRecord assetRecord) {
        this.assetRecord = assetRecord;
    }

    public AssetRecord assetRecord() {
        return assetRecord;
    }

    public ByteBuffer vbo() {
        return vbo;
    }

    public IntBuffer ebo() {
        return ebo;
    }
}

