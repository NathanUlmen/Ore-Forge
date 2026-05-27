package ore.forge.engine.render;


import com.badlogic.gdx.utils.Disposable;

public sealed interface GpuResource extends Disposable permits GpuMeshResource, GpuTextureResource {
}
