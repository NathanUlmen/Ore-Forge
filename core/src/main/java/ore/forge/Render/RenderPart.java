package ore.forge.Render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector4;

public class RenderPart {
    public MeshHandle mesh;
    public MaterialHandle material;
    public Matrix4 transform;
    public Vector4 tint, uvParams;
    public int flags; //derived from RenderFlags

    public RenderPart() {

        this.transform = new Matrix4();
    }

    public static RenderPart defaultRenderPart(MeshHandle handle) {
        RenderPart part = new RenderPart();
        part.mesh = handle;
        MaterialHandle mat = new MaterialHandle();
        part.material = mat;
        return part;
    }

    public String toString() {
        return mesh.toString();
    }

}

