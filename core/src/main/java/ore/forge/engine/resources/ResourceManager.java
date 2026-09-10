package ore.forge.engine.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.Handle;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.profiling.Stopwatch;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import ore.forge.engine.RenderThreadDispatcher;

/**
 * Public resource-system entry point for importing, registry persistence, CPU residency, and GPU residency.
 */
public class ResourceManager implements RenderThreadDispatcher {
    private final ConcurrentLinkedQueue<Runnable> workQueue;
    private final AssetRegistry registry;
    private final AssetImporter importer;
    private final AssetManager assetManager;
    private final GpuResourceManager gpuResourceManager;
    
    enum RequestType {
        SYNCHRONOUS,
        ASYNC
    }

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

    //---synchronous loading---   
    public ResourceHandle<CpuAssetData> acquireCpuData(AssetID id) {
        return assetManager.acquireResourceHandle(id, RequestType.SYNCHRONOUS);
    }

    public ResourceHandle<GpuResource> acquireGpuResource(AssetID id) {
        return gpuResourceManager.acquiResourceHandle(id, RequestType.SYNCHRONOUS);
    }

    public ResourceHandle<CpuAssetData> acquireCpuDataAsync(AssetID id) {
        return assetManager.acquireResourceHandle(id, RequestType.ASYNC);
    }

    public ResourceHandle<GpuResource> acquireGpuResourceAsync(AssetID id) {
        return gpuResourceManager.acquiResourceHandle(id, RequestType.ASYNC);
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

    @Override
    public boolean isRenderThread() {
        return true;
    }

}
