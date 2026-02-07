package ore.forge.Render;

import com.badlogic.gdx.math.collision.BoundingBox;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public record PackedMesh(FloatBuffer vertices,
                         IntBuffer indices,
                         int vertexCount,
                         int indexCount,
                         BoundingBox boundingBox) {
}
