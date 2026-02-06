package ore.forge.Render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector4;

public class RenderCommand {
    public final MeshHandle mesh;

    public final MaterialHandle material;

    public final Matrix4 worldTransform;
    public final Vector4 tint;
    public final Vector4 uvParams;

    public final int meshId;
    public final int materialId;

    public int flags;

    public RenderCommand(RenderPart part) {
        this.mesh = part.mesh;
        this.material = part.material;

        this.worldTransform = part.transform.cpy();

        this.tint = new Vector4(
            part.tint != null ? part.tint : new Vector4(1, 1, 1, 1)
        );

        this.uvParams = new Vector4(
            part.uvParams != null ? part.uvParams : new Vector4(1, 1, 0, 0)
        );

        this.meshId = System.identityHashCode(mesh);
        this.materialId = System.identityHashCode(material);

        flags = part.flags;
    }

}
