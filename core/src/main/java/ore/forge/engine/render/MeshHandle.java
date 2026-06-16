package ore.forge.engine.render;

import ore.forge.engine.Handle;


/**
 * Mesh Handle stores the index range for the target mesh that is to be drawn*/
public final class MeshHandle implements AssetHandle {
    public final Handle<GpuResource> handle;

    public MeshHandle(Handle<GpuResource> handle) {
        this.handle = handle;
    }


}
