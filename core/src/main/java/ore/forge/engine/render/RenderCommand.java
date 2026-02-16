package ore.forge.engine.render;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector4;
import com.badlogic.gdx.utils.Pool;

public class RenderCommand implements Pool.Poolable {
    public MeshHandle meshHandle;
    public MaterialHandle materialHandle;

    public final Matrix4 worldTransform;
    public final Vector4 tint;
    public final Vector4 uvParams;

    public int meshId;
    public int materialId;

    public int flags;

    public RenderCommand(RenderPart part) {
        this.meshHandle = part.meshHandle;
        this.materialHandle = part.material;

        this.worldTransform = part.transform.cpy();

        this.tint = new Vector4(
            part.tint != null ? part.tint : new Vector4(1, 1, 1, 1)
        );

        this.uvParams = new Vector4(
            part.uvParams != null ? part.uvParams : new Vector4(1, 1, 0, 0)
        );

        this.meshId = System.identityHashCode(meshHandle);
        this.materialId = System.identityHashCode(materialHandle);

        flags = part.flags;
    }

    public RenderCommand() {
        meshHandle = null;
        materialHandle = null;
        worldTransform = new Matrix4();
        tint = new Vector4(1,0,1,1);
        uvParams = new Vector4(1, 1, 0, 0);
    }

    @Override
    public void reset() {
        worldTransform.idt();
        meshId = -1;
        materialId = -1;
        flags = 0;
    }

    public void init(RenderPart part) {
        this.meshHandle = part.meshHandle;
        this.materialHandle = part.material;

        this.worldTransform.set(part.transform);

        if (part.tint != null) {
            this.tint.set(part.tint);
        }


        if (part.uvParams != null) {
            this.uvParams.set(part.uvParams);
        }

        this.meshId = System.identityHashCode(meshHandle);
        this.materialId = System.identityHashCode(materialHandle);

        flags = part.flags;
    }

}
