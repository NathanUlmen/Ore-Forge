package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.ComponentDefinition;
import ore.forge.engine.Handle;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.resources.AssetID;
import ore.forge.engine.resources.GpuResource;
import ore.forge.engine.resources.ResourceManager;
import ore.forge.engine.render.MaterialHandle;
import ore.forge.engine.render.RenderPart;

public class RenderCDefinition implements ComponentDefinition<RenderC> {
    private final AssetID meshId;
    private final AssetID materialId;
    private final Vector3 scale;
    private final Matrix4 localFromEntity;
    private final ResourceManager resourceManager;

    public RenderCDefinition(
        AssetID meshHandle,
        AssetID material,
        Vector3 scale,
        Matrix4 localFromEntity,
        ResourceManager resourceManager
    ) {
        this.resourceManager = resourceManager;
        this.meshId = meshHandle;
        this.materialId = material;
        this.scale = new Vector3(scale);
        this.localFromEntity = new Matrix4(localFromEntity);
    }

    @Override
    public RenderC create() {
        RenderC component = new RenderC();
        RenderPart part = RenderPart.defaultRenderPart(resourceManager.getGpuHandle(meshId));
        part.material.baseColorTexture = resourceManager.getGpuHandle(materialId);
        component.renderPart = part;
        component.scale.set(scale);
        component.localFromEntity.set(localFromEntity);
        return component;
    }

}
