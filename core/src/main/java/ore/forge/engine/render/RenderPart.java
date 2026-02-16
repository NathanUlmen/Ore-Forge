package ore.forge.engine.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector4;

public class RenderPart {
    public MeshHandle meshHandle;
    public MaterialHandle material;
    public Matrix4 transform; //final transform, used for instancing
    public Vector4 tint, uvParams;
    public int flags; //derived from RenderFlags

    public RenderPart() {
        this.transform = new Matrix4();
    }

    public static RenderPart defaultRenderPart(MeshHandle handle) {
        RenderPart part = new RenderPart();
        part.meshHandle = handle;
        part.material = new MaterialHandle();
        return part;
    }

    public String toString() {
        return meshHandle.toString();
    }

}

