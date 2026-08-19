package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.ComponentDefinition;
import ore.forge.engine.Handle;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.resources.GpuResource;
import ore.forge.engine.render.MaterialHandle;
import ore.forge.engine.render.RenderPart;

public class RenderCDefinition implements ComponentDefinition<RenderC> {
    private final Handle<GpuResource> meshHandle;
    private final MaterialHandle material;
    private final Vector3 scale;
    private final Matrix4 localFromEntity;

    public RenderCDefinition(
        Handle<GpuResource> meshHandle,
        MaterialHandle material,
        Vector3 scale,
        Matrix4 localFromEntity
    ) {
        this.meshHandle = meshHandle;
        this.material = material;
        this.scale = new Vector3(scale);
        this.localFromEntity = new Matrix4(localFromEntity);
    }

    @Override
    public RenderC create() {
        RenderC component = new RenderC();
        RenderPart part = RenderPart.defaultRenderPart(meshHandle);
        part.material = material;
        component.renderPart = part;
        component.scale.set(scale);
        component.localFromEntity.set(localFromEntity);
        return component;
    }

}
