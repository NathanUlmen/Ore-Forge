package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;

import java.util.Arrays;

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
        return (long) vbo.length * Float.BYTES + (long) ibo.length * Short.BYTES;
    }

    @Override
    public void dispose() {
        if (vbo != null && ibo != null) {
            vbo = null;
            ibo = null;
            Gdx.app.log("MeshData", "Disposed MeshData");
        } else {
            throw new IllegalStateException("MeshData was already disposed");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MeshData other) {
            return attributes.equals(other.attributes) && Arrays.equals(vbo, other.vbo) && Arrays.equals(ibo, other.ibo);
        }
        return false;
    }

}
