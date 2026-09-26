package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.LongMap;
import ore.forge.engine.*;
import ore.forge.engine.resources.ResourceSlot.LoadState;

import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class AssetManager {
    private static final String LOG_TAG = AssetManager.class.getName();
    // Native-free 1x1 PNG placeholder so CPU-only tests do not require libGDX image natives at class load time.
    private static final byte[] DEFAULT_TEXTURE_BYTES = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR42mP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
    );
    private final HashMap<AssetID, Handle<CpuAssetData>> handleLookup;
    private final LongMap<AssetID> removeLookup; //maps handles to their asset id.
    private final HashMap<AssetID, CompletableFuture<CpuAssetData>> cpuReadyFutures;
    private final HandleRegistry<CpuAssetData> handleRegistry;
    final Cache<AssetID, CpuAssetData> cache;
    private final AssetRegistry assetRegistry;
    private final AssetDataSerializer serializer;
    private final Dispatcher dispatcher;

    public AssetManager(AssetRegistry registry, Dispatcher dispatcher, long cachSize) {
        this.cache = new CacheLRU<>(cachSize);
        this.cpuReadyFutures = new HashMap<>();
        this.assetRegistry = registry;
        this.handleLookup = new HashMap<>();
        this.handleRegistry = new HandleRegistry<>();
        this.serializer = new AssetDataSerializer();
        this.dispatcher  = dispatcher;
        this.removeLookup = new LongMap<>();
    }

    public ResourceHandle<CpuAssetData> acquireResourceHandle(AssetID id, Consumer<ResourceHandle<CpuAssetData>> callback, Dispatcher callbackDispatcher) {
        Handle<CpuAssetData> lookupHandle = handleLookup.get(id);
        if (lookupHandle != null) { //case 1: target is already loaded or is in flight.
            ResourceHandle<CpuAssetData> assetData = new ResourceHandle<>(handleRegistry.accquireHandle(lookupHandle), this.getCpuReadyFuture(id));
            cpuReadyFutures.replace(id, scheduleCallback(callback, assetData, callbackDispatcher, this.getCpuReadyFuture(id)));
            return assetData;
        }

        //case 2: in cache and needs to be added
        CpuAssetData cacheLookup = cache.take(id);
        if (cacheLookup != null) {
            Handle<CpuAssetData> handle = handleRegistry.addResource(cacheLookup, LoadState.COMPLETED);
            handleLookup.put(id, handle);
            removeLookup.put(handle.identity(), id);
            var cpuReadyFuture = CompletableFuture.completedFuture(cacheLookup);
            var resourceHandle = new ResourceHandle<>(handle, cpuReadyFuture);
            cpuReadyFutures.put(id, scheduleCallback(callback, resourceHandle, callbackDispatcher, cpuReadyFuture));
            return resourceHandle;
        }


        //case 3: load has not been requested
        ore.forge.engine.resources.AssetArtifact target = assetRegistry.lookUp(id);
        if (target == null) {
            IllegalArgumentException e = new IllegalArgumentException();
            Gdx.app.error(LOG_TAG, "Target artifact of id:[" + id + "] was not present in asset registry.", e);
            throw e;
        }

        //reserve a slot in the registry and populate it with a placeholder
        Handle<CpuAssetData> handle = handleRegistry.addResource(this.resolvePlaceHolder(target), LoadState.REQUESTED);

        //create link between id and handle
        handleLookup.put(id, handle);
        removeLookup.put(handle.identity(), id);

        ResourceSlot<CpuAssetData> slot = handleRegistry.getResourceSlot(handle);
        CompletableFuture<CpuAssetData> loadFuture = serializer.load(target, slot);

        CompletableFuture<CpuAssetData> cpuReady = new CompletableFuture<>();
        cpuReadyFutures.put(id, cpuReady);
        var resourceHandle = new ResourceHandle<>(handle, cpuReady);

        loadFuture.thenAcceptAsync(loadedData -> {
            resolveLoad(id, cpuReady, slot, loadedData);
        }, this.dispatcher::post);

        scheduleCallback(callback, resourceHandle, callbackDispatcher, loadFuture);

        if (target.dependencies() != null){
            for (ore.forge.engine.resources.AssetArtifact dependency : target.dependencies()) {
                acquireResourceHandle(dependency.assetID(), null, null);
            }
        }

        return resourceHandle;
    }

    private void resolveLoad(AssetID id, CompletableFuture<CpuAssetData> cpuReady, ResourceSlot<CpuAssetData> slot, CpuAssetData result) {
        if (slot != null) {
            slot.resolve(result);
            slot.setLoadState(LoadState.COMPLETED);
            cpuReady.complete(result);
        } else {
            cache.put(id, result);
            cpuReady.cancel(false);
        }
        cpuReadyFutures.remove(id);
    }

    /**
     * Will return a completable future. the future is completed if it has already resolved. if the future is still in progress will return that one instead
     * @param id target
     * @return a complete future if the value has already been resolved or an in progress one if still in the process. returns null if
     */
    public CompletableFuture<CpuAssetData> getCpuReadyFuture(AssetID id) {
        var handle = handleLookup.get(id);
        if (handle != null && getSlot(handle).isResolved()) { //future has already been completed and we are no longer tracking it
            return CompletableFuture.completedFuture(handleRegistry.getResource(handle));
        }
        return cpuReadyFutures.get(id);
    }

    private CompletableFuture<CpuAssetData> scheduleCallback(Consumer<ResourceHandle<CpuAssetData>> callback, ResourceHandle<CpuAssetData> data, Dispatcher dispatcher, CompletableFuture<CpuAssetData> future) {
        if (callback != null && dispatcher == null) {throw new IllegalArgumentException("Dispatcher cannot be null.");}
        if (callback != null) {
            return future.thenApplyAsync((ignored) -> {
                callback.accept(data);
                return ignored;
            }, dispatcher::post);
        }
        return future;
    }

    public ResourceSlot<CpuAssetData> getSlot(Handle<CpuAssetData> handle) {
        return handleRegistry.getResourceSlot(handle);
    }

    public void releaseHandle(Handle<CpuAssetData> handle) {
        long handleIdentity = handle.identity();
        CpuAssetData data = handleRegistry.getResource(handle);
        if (handleRegistry.releaseHandle(handle)) {
            AssetID id = removeLookup.remove(handleIdentity);
            if (id != null) {
                cache.put(id, data);
                handleLookup.remove(id);
                cpuReadyFutures.remove(id);
            }
        }
    }

    public CpuAssetData resolvePlaceHolder(AssetArtifact target) {
        return switch (target.type()) {
            case MESH -> createDefaultMesh();
            case TEXTURE -> createDefaultTexture();
            case MATERIAL, ANIMATION ->
                throw new UnsupportedOperationException("No placeholder is defined for asset type: " + target.type());
        };
    }

    public CpuAssetData resolveHandle(Handle<CpuAssetData> handle) {
        return handleRegistry.getResource(handle);
    }

    public int size() {
        return handleRegistry.size();
    }

    private static MeshData createDefaultMesh() {
        VertexAttributes attributes = new VertexAttributes(
            VertexAttribute.POSITION.toGdxAttribute(),
            VertexAttribute.NORMAL.toGdxAttribute(),
            VertexAttribute.TEXCOORD_0.toGdxAttribute()
        );

        float[] vbo = new float[] {
            -0.5f, -0.5f,  0.5f,  0f,  0f,  1f,  0f, 0f,
             0.5f, -0.5f,  0.5f,  0f,  0f,  1f,  1f, 0f,
             0.5f,  0.5f,  0.5f,  0f,  0f,  1f,  1f, 1f,
            -0.5f,  0.5f,  0.5f,  0f,  0f,  1f,  0f, 1f,

             0.5f, -0.5f, -0.5f,  0f,  0f, -1f,  0f, 0f,
            -0.5f, -0.5f, -0.5f,  0f,  0f, -1f,  1f, 0f,
            -0.5f,  0.5f, -0.5f,  0f,  0f, -1f,  1f, 1f,
             0.5f,  0.5f, -0.5f,  0f,  0f, -1f,  0f, 1f,

            -0.5f, -0.5f, -0.5f, -1f,  0f,  0f,  0f, 0f,
            -0.5f, -0.5f,  0.5f, -1f,  0f,  0f,  1f, 0f,
            -0.5f,  0.5f,  0.5f, -1f,  0f,  0f,  1f, 1f,
            -0.5f,  0.5f, -0.5f, -1f,  0f,  0f,  0f, 1f,

             0.5f, -0.5f,  0.5f,  1f,  0f,  0f,  0f, 0f,
             0.5f, -0.5f, -0.5f,  1f,  0f,  0f,  1f, 0f,
             0.5f,  0.5f, -0.5f,  1f,  0f,  0f,  1f, 1f,
             0.5f,  0.5f,  0.5f,  1f,  0f,  0f,  0f, 1f,

            -0.5f,  0.5f,  0.5f,  0f,  1f,  0f,  0f, 0f,
             0.5f,  0.5f,  0.5f,  0f,  1f,  0f,  1f, 0f,
             0.5f,  0.5f, -0.5f,  0f,  1f,  0f,  1f, 1f,
            -0.5f,  0.5f, -0.5f,  0f,  1f,  0f,  0f, 1f,

            -0.5f, -0.5f, -0.5f,  0f, -1f,  0f,  0f, 0f,
             0.5f, -0.5f, -0.5f,  0f, -1f,  0f,  1f, 0f,
             0.5f, -0.5f,  0.5f,  0f, -1f,  0f,  1f, 1f,
            -0.5f, -0.5f,  0.5f,  0f, -1f,  0f,  0f, 1f
        };

        short[] ibo = new short[] {
            0, 1, 2, 2, 3, 0,
            4, 5, 6, 6, 7, 4,
            8, 9, 10, 10, 11, 8,
            12, 13, 14, 14, 15, 12,
            16, 17, 18, 18, 19, 16,
            20, 21, 22, 22, 23, 20
        };

        return new MeshData(attributes, vbo, ibo);
    }

    private static TextureData createDefaultTexture() {
        return new TextureData(DEFAULT_TEXTURE_BYTES, false);
    }

}
