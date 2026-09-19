package ore.forge.engine.resources;


import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.Sizeable;

public sealed interface GpuResource extends Disposable, Sizeable permits GpuMeshResource, GpuTextureResource {
}
