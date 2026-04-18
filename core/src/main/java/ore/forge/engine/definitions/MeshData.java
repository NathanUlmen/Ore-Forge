package ore.forge.engine.definitions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

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
public record MeshData(AssetRecord record, FloatBuffer vbo, IntBuffer ebo) {
}

