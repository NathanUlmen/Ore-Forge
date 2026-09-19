package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.utils.LongMap;
import ore.forge.engine.Handle;
import ore.forge.engine.HandleRegistry;
import ore.forge.engine.Sizeable;
import ore.forge.engine.CacheLRU;
import ore.forge.engine.Dispatcher;
import ore.forge.engine.render.Renderer;
import ore.forge.engine.resources.ResourceSlot.LoadState;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Nathan Ulmen
 * Acts as an interface that the {@link Renderer} and other systems can interact with to reference Assets.
 * It ensures uniqueness among assets, preventing multiple instances of the same Asset.
 * It handles the loading of asset dependencies.
 *
 *
 */
final class GpuResourceManager {
    private static final String LOG_TAG = GpuResourceManager.class.getName();
    private final AssetManager assetManager;
    private final HashMap<AssetID, Handle<GpuResource>> handleLookup;
    private final LongMap<AssetID> removeLookup;
    private final HandleRegistry<GpuResource> gpuResources;
    private final HashMap<AssetID, CompletableFuture<GpuResource>> gpuReadyFutures;
    private final CacheLRU<AssetID, GpuResource> cache; 
    private final Dispatcher gpuDispatcher;
    private static final long CACHE_SIZE = 100 * Sizeable.MB;

    public GpuResourceManager(AssetManager assetManager, Dispatcher gpuDispatcher) {
        this.gpuDispatcher = gpuDispatcher;
        this.assetManager = assetManager;
        this.handleLookup = new HashMap<>();
        this.gpuResources = new HandleRegistry<>();
        this.gpuReadyFutures = new HashMap<>();
        this.removeLookup = new LongMap<>();
        this.cache = new CacheLRU<>(CACHE_SIZE);
    }

    /**
     * Given an {@link AssetID} the {@link GpuResourceManager} will return a handle to the
     * referenced asset. If the referenced asset is not currently in memory it will be loaded from disk
     * and then uploaded to the GPU.
     *
     * @param id to an asset that want a handle to.
     * @return A handle to the asset that the id references.
     */
    public ResourceHandle<GpuResource> acquireResourceHandle(AssetID id, Consumer<ResourceHandle<GpuResource>> callback, Dispatcher callbackDispatcher) {
        //case 1: already in progress or loaded.
        Handle<GpuResource> lookupHandle = handleLookup.get(id);
        if (lookupHandle != null) {
            ResourceHandle<GpuResource> resource = new ResourceHandle<>(gpuResources.accquireHandle(lookupHandle), gpuReadyFutures.get(id));
            gpuReadyFutures.replace(id, scheduleCallback(callback, resource, callbackDispatcher, gpuReadyFutures.get(id)));
            return resource;
        }
        
        //case 2: already in cache
        GpuResource cachedResource = cache.take(id);
        if (cachedResource != null) {
            Handle<GpuResource> handle = gpuResources.addResource(cachedResource, LoadState.COMPLETED);
            var completedFuture = CompletableFuture.completedFuture(cachedResource);
            removeLookup.put(handle.identity(), id);
            handleLookup.put(id, handle);

            var resourceHandle = new ResourceHandle<>(handle, completedFuture);
            gpuReadyFutures.put(id, scheduleCallback(callback, resourceHandle, callbackDispatcher, completedFuture));
            return resourceHandle;
        } 

        CompletableFuture<CpuAssetData> cpuReadyFuture = assetManager.getCpuReadyFuture(id);
        if (cpuReadyFuture == null) {//case 2: cpu not loaded at all
            //begin process of load

            Handle<GpuResource> handle = gpuResources.addResource(null, LoadState.REQUESTED);
            putHandle(id, handle);


            CompletableFuture<GpuResource> future = new CompletableFuture<>();
            ResourceHandle<CpuAssetData> cpuResourceHandle = assetManager.acquireResourceHandle(id, null, null);
            CompletableFuture<GpuResource> gpuResourceFuture = cpuResourceHandle.getFuture().thenApplyAsync((loadedData) -> {
                ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);
                if (slot != null) {
                    var gpuResource = createGpuResource(id, loadedData);
                    slot.resolve(gpuResource);
                    slot.setLoadState(LoadState.COMPLETED);
                    future.complete(gpuResource);
                    assetManager.releaseHandle(cpuResourceHandle.handle()); //Free as we no longer need it.
                    return gpuResource;
                }
                assetManager.releaseHandle(cpuResourceHandle.handle()); //Free as we no longer need it.
                return null;
            }, this.gpuDispatcher::post);

            ResourceHandle<GpuResource> resource = new ResourceHandle<>(handle, future);
            gpuReadyFutures.put(id, scheduleCallback(callback, resource, callbackDispatcher, gpuResourceFuture));

            return resource;
        } else if (!cpuReadyFuture.isDone()) {//case 3: cpu in progress
            Handle<GpuResource> handle = gpuResources.addResource(null, LoadState.REQUESTED);
            ResourceHandle<CpuAssetData> cpuAssetDataHandle = assetManager.acquireResourceHandle(id, null, null);
            putHandle(id, handle);
            CompletableFuture<GpuResource> loadedFuture = cpuReadyFuture.thenApplyAsync((loadedData) -> {
                ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);
                if (slot != null) {
                    var gpuResource = createGpuResource(id, assetManager.resolveHandle(cpuAssetDataHandle.handle()));
                    slot.resolve(gpuResource);
                    slot.setLoadState(LoadState.COMPLETED);
                    assetManager.releaseHandle(cpuAssetDataHandle.handle());
                    return gpuResource;
                }
                assetManager.releaseHandle(cpuAssetDataHandle.handle());
                return null;
            }, this.gpuDispatcher::post);

            ResourceHandle<GpuResource> resource = new ResourceHandle<GpuResource>(handle, loadedFuture);
            gpuReadyFutures.put(id, scheduleCallback(callback, resource, callbackDispatcher, loadedFuture));

            return resource;
        } else {//case 4: cpu side already loaded.
            Handle<GpuResource> handle = createHandleToResource(null, LoadState.IN_PROGRESS);
            ResourceHandle<CpuAssetData> cpuAssetDataHandle = assetManager.acquireResourceHandle(id, null, null);
            CompletableFuture<GpuResource> future = new CompletableFuture<>();
            ResourceHandle<GpuResource> resource = new ResourceHandle<>(handle, future);

            gpuReadyFutures.put(id, future);
            this.gpuDispatcher.post(() -> {
                ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);
                if (slot != null) {
                    CpuAssetData cpuData = assetManager.resolveHandle(cpuAssetDataHandle.handle());
                    GpuResource gpuResource = createGpuResource(id, cpuData);
                    slot.resolve(gpuResource);
                    slot.setLoadState(LoadState.COMPLETED);
                    future.complete(gpuResource);
                    gpuReadyFutures.replace(id, scheduleCallback(callback, resource, callbackDispatcher, future));
                }
                assetManager.releaseHandle(cpuAssetDataHandle.handle());
            });

            //log that resource exists
            putHandle(id, handle);

            return resource;
        }
    }

    private CompletableFuture<GpuResource> scheduleCallback(Consumer<ResourceHandle<GpuResource>> callback, ResourceHandle<GpuResource> data, Dispatcher dispatcher, CompletableFuture<GpuResource> future) {
        if (callback != null && dispatcher == null) {
            throw new IllegalArgumentException("Dispatcher cannot be null.");
        }
        if (callback != null) {
            return future.thenApplyAsync((ignored) -> {
                callback.accept(data);
                return ignored;
            }, dispatcher::post);
        }
        return future;
    }

    private void putHandle(AssetID id, Handle<GpuResource> handle) {
        handleLookup.put(id, handle);
        removeLookup.put(handle.identity(), id);
    }

    private GpuResource createGpuResource(AssetID id, CpuAssetData data) {
        return switch (data) {
            case MeshData meshData -> uploadMesh(id, meshData);
            case TextureData textureData -> uploadTexture(id, textureData);
            case MaterialData materialData ->
                throw new UnsupportedOperationException("Material upload not implemented yet.");
            case AnimationData animationData ->
                throw new UnsupportedOperationException("Animation upload not implemented yet.");
        };
    }

    public void releaseHandle(Handle<GpuResource> handle) {
        this.gpuDispatcher.post(() -> {
            long handleIdentity = handle.identity();
            ResourceSlot<GpuResource> slot  = gpuResources.getResourceSlot(handle);
            GpuResource gpuResource = gpuResources.getResource(handle);
            if (gpuResources.releaseHandle(handle)) {
                AssetID id = removeLookup.remove(handleIdentity);

                if (slot != null && !slot.isResolved()) {
                    gpuReadyFutures.get(id).cancel(false);
                }

                if (id != null) {
                    cache.put(id, gpuResource);
                    handleLookup.remove(id);
                    gpuReadyFutures.remove(id);
                }
            }
        });
    }

    private Handle<GpuResource> createHandleToResource(GpuResource resource, LoadState state) {
        return gpuResources.addResource(resource, state);
    }

    /**
     *
     */
    private GpuResource uploadMesh(AssetID id, MeshData meshData) {
        float[] vertices = meshData.vbo();
        short[] indices = meshData.ibo();

        if (vertices == null || indices == null) {
            throw new IllegalStateException("MeshData must have vertices or indices. ID={" + id + "}");
        }

        VertexBufferObjectWithVAO vbo = new VertexBufferObjectWithVAO(
            true,
            vertices.length,
            meshData.attributes()
        );
        vbo.setVertices(vertices, 0, vertices.length);

        IndexBufferObject ibo = new IndexBufferObject(indices.length);
        ibo.setIndices(indices, 0, indices.length);

        GpuMeshResource meshResource = new GpuMeshResource(
            vbo,
            ibo,
            vertices.length,
            indices.length,
            GL20.GL_UNSIGNED_SHORT,
            0
        );

        return meshResource;
    }

    /**
     * Uploads {@link Pixmap} data to the GPU to be used as a texture. If the
     * {@link Pixmap} has not been constructed from the encoded bytes yet, that operation will
     * be performed.
     *
     * @param id to be mapped to the {@link }.
     * @return TextureHandle that points to the {@link GpuTextureResource}
     */
    private GpuResource uploadTexture(AssetID id, TextureData textureData) {
        Pixmap map = textureData.pixmap();
        GpuTextureResource textureResource = new GpuTextureResource(map, textureData.useMipMaps());

        return textureResource;
    }

    /**
     * Used to reference resources stored on the GPU
     *
     * @param assetHandle Handle to the resource on that you want to reference on that's stored on the GPU.
     * @return resource that the assetHandle references.
     */
    public GpuResource resolveHandle(Handle<GpuResource> assetHandle) {
        return gpuResources.getResource(assetHandle);
    }

    public int resourceCount() {
        return gpuResources.size();
    }

    public String toString() {
        return "";
    }

}
