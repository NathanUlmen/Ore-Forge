package ore.forge.engine.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.Handle;
import ore.forge.engine.definitions.AssetType;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import ore.forge.engine.Dispatcher;

/**
 * Public resource-system entry point for importing, registry persistence, CPU residency, and GPU residency.
 */
public class ResourceManager implements Dispatcher {
    private final ConcurrentLinkedQueue<Runnable> workQueue;
    private final AssetRegistry registry;
    private final AssetImporter importer;
    private final AssetManager assetManager;
    private final GpuResourceManager gpuResourceManager;

    public ResourceManager() {
        this(new AssetRegistry());
    }

    public ResourceManager(String bakedOutputDir) {
        this(new AssetRegistry(bakedOutputDir));
    }

    private ResourceManager(AssetRegistry registry) {
        this.registry = registry;
        this.importer = new AssetImporter(registry);
        this.assetManager = new AssetManager(registry, this);
        this.gpuResourceManager = new GpuResourceManager(assetManager, this);
        this.workQueue = new ConcurrentLinkedQueue<>();
    }

    public void importGltf(Path file) {
        importer.importGlbFile(file);
    }

    public CpuAssetData getCpuAsset(Handle<CpuAssetData> handle) {
        return assetManager.resolveHandle(handle);
    }

    public GpuResource getGpuResource(Handle<GpuResource> assetHandle) {
        return gpuResourceManager.resolveHandle(assetHandle);
    }

    public AssetType getAssetType(AssetID id) {
        return registry.requireArtifact(id).sourceKey().assetType();
    }

    public Iterable<AssetID> getAssetIDs() {
        return registry.getIDs();
    }

    public void saveRegistry(Path outputFile) {
        registry.save(outputFile.toFile());
    }

    public void loadRegistry(JsonValue jsonValue) {
        registry.load(jsonValue);
    }

    public void loadRegistry(FileHandle fileHandle) {
        loadRegistry(new JsonReader().parse(fileHandle));
    }

    public void releaseGpuResource(ResourceHandle<GpuResource> handle) {
        gpuResourceManager.releaseHandle(handle.handle());
    }

    public int activeCpuResources() {
        return assetManager.size();
    }

    public int activeGpuResources() {
        return gpuResourceManager.resouceCount();
    }

    public ResourceHandle<CpuAssetData> acquireCpuDataAsync(AssetID id, Consumer<ResourceHandle<CpuAssetData>> callback, Dispatcher callbackDispatcher) {
        return assetManager.acquireResourceHandle(id, callback, callbackDispatcher);
    }

    public ResourceHandle<GpuResource> acquireGpuResourceAsync(AssetID id, Consumer<ResourceHandle<GpuResource>> callback, Dispatcher callbackDispatcher) {
        return gpuResourceManager.acquiResourceHandle(id, callback, callbackDispatcher);
    }

    public void synchronize() {
        Runnable runnable = workQueue.poll();
        while (runnable != null) {
            runnable.run();
            runnable = workQueue.poll();
        }
    }

    @Override
    public void post(Runnable runnable) {
        workQueue.add(runnable);
    }

}
