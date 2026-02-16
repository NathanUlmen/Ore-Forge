package ore.forge.engine.render;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public record PackedMesh(FloatBuffer vertices,
                         IntBuffer indices,
                         int vertexCount,
                         int indexCount,
                         BoundingBox boundingBox, VertexAttributes attributes, String id) {
}
