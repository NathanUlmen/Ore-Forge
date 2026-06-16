package ore.forge.engine.render;

import ore.forge.engine.Handle;

public final class TextureHandle implements AssetHandle {
    final Handle<GpuResource> handle;

    public TextureHandle(Handle<GpuResource> handle) {
        this.handle = handle;
    }

}
