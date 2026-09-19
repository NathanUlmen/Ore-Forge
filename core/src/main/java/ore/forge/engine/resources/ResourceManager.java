package ore.forge.engine.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.Handle;
import ore.forge.engine.definitions.AssetType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import ore.forge.engine.Dispatcher;
import ore.forge.engine.profiling.Stopwatch;

/**
 * By default, dispatches to self.
 */
public class ResourceManager implements Dispatcher {
    private final ConcurrentLinkedQueue<Runnable> workQueue;
    private final AssetRegistry registry;
    private final AssetImporter importer;
    private final AssetManager assetManager;
    private final GpuResourceManager gpuResourceManager;
    private final Dispatcher dispatcher;

    public ResourceManager() {
        this(new AssetRegistry(), null);
    }

    public ResourceManager(Dispatcher dispatcher) {
        this(new AssetRegistry(), dispatcher);
    }

    public ResourceManager(String bakedOutputDir) {
        this(new AssetRegistry(bakedOutputDir), null);
    }

    public ResourceManager(String bakedOutputDir, Dispatcher dispatcher) {
        this(new AssetRegistry(bakedOutputDir), dispatcher);
    }

    private ResourceManager(AssetRegistry registry, Dispatcher dispatcher) {
        this.dispatcher = dispatcher == null ? this : dispatcher;
        this.registry = registry;
        this.importer = new AssetImporter(registry);
        this.assetManager = new AssetManager(registry, this.dispatcher);
        this.gpuResourceManager = new GpuResourceManager(assetManager, this.dispatcher);
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

    public void releaseCpuAsset(ResourceHandle<CpuAssetData> handle) {
        assetManager.releaseHandle(handle.handle());
    }

    public int activeCpuResources() {
        return assetManager.size();
    }

    public int activeGpuResources() {
        return gpuResourceManager.resourceCount();
    }

    public ResourceHandle<CpuAssetData> acquireCpuDataAsync(AssetID id) {
        return assetManager.acquireResourceHandle(id, null, null);
    }

    public ResourceHandle<GpuResource> acquireGpuResourceAsync(AssetID id) {
        return gpuResourceManager.acquireResourceHandle(id, null, null);
    }

    public ResourceHandle<CpuAssetData> acquireCpuDataAsync(AssetID id, Consumer<ResourceHandle<CpuAssetData>> callback, Dispatcher callbackDispatcher) {
        return assetManager.acquireResourceHandle(id, callback, callbackDispatcher);
    }

    public ResourceHandle<GpuResource> acquireGpuResourceAsync(AssetID id, Consumer<ResourceHandle<GpuResource>> callback, Dispatcher callbackDispatcher) {
        return gpuResourceManager.acquireResourceHandle(id, callback, callbackDispatcher);
    }

    public void batchLoad(List<AssetID.BatchRequest> requests, Consumer<Collection<ResourceHandle<?>>> callback, Dispatcher callbackDispatcher) {
        List<ResourceHandle<?>> resources = new ArrayList<>();
        CompletableFuture<?>[] handles = new CompletableFuture[requests.size()];
        for (int i = 0; i < requests.size(); i++) {
            AssetID.BatchRequest request = requests.get(i);
            ResourceHandle<?> handle = null;
            switch (request.type()) {
                case CPU_DATA -> handle = assetManager.acquireResourceHandle(request.id(), null, null);
                case GPU_RESOURCE -> handle = gpuResourceManager.acquireResourceHandle(request.id(), null, null);
            }
            handles[i] = handle.getFuture();
            resources.add(handle);
        }
        CompletableFuture.allOf(handles).thenRunAsync(() -> {
            callback.accept(resources);
        }, callbackDispatcher::post);
    }

    public void setCpuCacheMaxBytes(long newMax) {
        this.dispatcher.post(() -> {
            assetManager.cache.setMaxSizeBytes(newMax);
        });
    }

    public void synchronize() {
        Runnable runnable = workQueue.poll();
        long start = Stopwatch.timeNow(TimeUnit.MICROSECONDS);
        while (runnable != null) {
            runnable.run();
            if (Stopwatch.timeNow(TimeUnit.MICROSECONDS) - start >= 500) {
                break;
            }
            runnable = workQueue.poll();
        }
    }

    public int queuedTaskCount() {
        return workQueue.size();
    }

    @Override
    public void post(Runnable runnable) {
        workQueue.add(runnable);
    }

}
