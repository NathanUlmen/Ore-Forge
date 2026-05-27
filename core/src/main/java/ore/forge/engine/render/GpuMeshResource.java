package ore.forge.engine.render;

import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;

public final class GpuMeshResource implements GpuResource {
    private final VertexBufferObjectWithVAO vertexBufferObject;
    private final IndexBufferObject indexBufferObject;
    private final int floatVertexCount;
    private final int indexCount;
    private final int indexType;
    private final int indexOffsetBytes;

    public GpuMeshResource(VertexBufferObjectWithVAO vbo, IndexBufferObject ibo, int floatVertexCount,
                           int indexCount, int indexType, int indexOffsetBytes) {
        this.vertexBufferObject = vbo;
        this.indexBufferObject = ibo;
        this.floatVertexCount = floatVertexCount;
        this.indexCount = indexCount;
        this.indexType = indexType;
        this.indexOffsetBytes = indexOffsetBytes;
    }

    public int indexCount() {
        return indexCount;
    }

    public int indexType() {
        return indexType;
    }

    public int indexOffsetBytes() {
        return indexOffsetBytes;
    }

    public VertexBufferObjectWithVAO vbo() {
        return vertexBufferObject;
    }

    public IndexBufferObject ibo() {
        return indexBufferObject;
    }

    @Override
    public void dispose() {
        vertexBufferObject.dispose();
        indexBufferObject.dispose();
    }
}
