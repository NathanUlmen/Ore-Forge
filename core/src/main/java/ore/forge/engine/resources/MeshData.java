package ore.forge.engine.resources;

import com.badlogic.gdx.graphics.VertexAttributes;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * @author Nathan Ulmen
 * Raw Mesh data before being uploaded to GPU.
 */
public final class MeshData implements CpuAssetData {
    private final VertexAttributes attributes;
    private float[] vbo;
    private short[] ibo;

    public MeshData(VertexAttributes attributes, float[] vbo, short[] ibo) {
        this.attributes = attributes;
        this.vbo = vbo;
        this.ibo = ibo;
    }

    public float[] vbo() {
        return vbo;
    }

    public short[] ibo() {
        return ibo;
    }

    public VertexAttributes attributes() {
        return attributes;
    }

    @Override
    public long sizeInBytes() {
        return vbo.length * Float.BYTES + ibo.length * Short.BYTES;
    }

    @Override
    public void dispose() {
        vbo = null;
        ibo = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MeshData other) {
            return attributes.equals(other.attributes) && Arrays.equals(vbo, other.vbo) && Arrays.equals(ibo, other.ibo);
        }
        return false;
    }

}
