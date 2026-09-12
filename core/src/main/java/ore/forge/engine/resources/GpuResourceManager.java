package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import ore.forge.engine.Handle;
import ore.forge.engine.HandleRegistry;
import ore.forge.engine.Pair;
import ore.forge.engine.Dispatcher;
import ore.forge.engine.render.Renderer;
import ore.forge.engine.resources.ResourceSlot.LoadState;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

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
    private final HashMap<AssetID, Handle<GpuResource>> handles;
    private final HandleRegistry<GpuResource> gpuResources;
    private final HashMap<AssetID, CompletableFuture<?>> gpuReadyFutures;
    private final Dispatcher dispatcher;

    public GpuResourceManager(AssetManager assetManager, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.assetManager = assetManager;
        this.handles = new HashMap<>();
        this.gpuResources = new HandleRegistry<>();
        this.gpuReadyFutures = new HashMap<>();
    }

    /**
     * Given an {@link AssetID} the {@link GpuResourceManager} will return a handle to the
     * referenced asset. If the referenced asset is not currently in memory it will be loaded from disk
     * and then uploaded to the GPU.
     *
     * @param id to an asset that want a handle to.
     * @return A handle to the asset that the id references.
     */
    public ResourceHandle<GpuResource> acquiResourceHandle(AssetID id, Consumer<ResourceHandle<GpuResource>> callback, Dispatcher callbackDispatcher) {
        //case 1: already in progress or loaded.
        Handle<GpuResource> lookupHandle = handles.get(id);
        if (lookupHandle != null) {
            ResourceHandle<GpuResource> resource = new ResourceHandle<>(gpuResources.accquireHandle(lookupHandle), gpuReadyFutures.get(id));
            scheduleCallback(callback, resource, callbackDispatcher, null);
            return resource;
        }

        CompletableFuture<CpuAssetData> cpuReadyFuture = assetManager.getCpuReadyFuture(id);
        if (cpuReadyFuture == null) {//case 2: cpu not loaded at all
            //begin process of load ;

            Handle<GpuResource> handle = gpuResources.addResource(null, LoadState.REQUESTED);
            handles.put(id, handle);
            ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);

            CompletableFuture<GpuResource> future = assetManager.getCpuReadyFuture(id).thenApplyAsync((loadedHandle) -> {
                slot.resolve(createGpuResouce(id, loadedHandle));
                slot.setLoadState(LoadState.COMPLETED);
                ResourceHandle<CpuAssetData> cpuResourceHandle = assetManager.acquireResourceHandle(id, null, null);
                //create resource
                return createGpuResouce(id, assetManager.resolveHandle(cpuResourceHandle.handle()));
            }, this.dispatcher::post);

            ResourceHandle<GpuResource> resource = new ResourceHandle<>(handle, future);

            scheduleCallback(callback, resource, callbackDispatcher, future);

            gpuReadyFutures.put(id, future);
            return resource;
        } else if (!cpuReadyFuture.isDone()) {//case 3: cpu in progress
            Handle<GpuResource> handle = gpuResources.addResource(null, LoadState.REQUESTED);
            handles.put(id, handle);

            ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);
            CompletableFuture<CpuAssetData> loadedFuture = cpuReadyFuture.thenApplyAsync((loadedData) -> {
                slot.resolve(createGpuResouce(id, loadedData));
                slot.setLoadState(LoadState.COMPLETED);
                return loadedData;
            }, this.dispatcher::post);

            ResourceHandle<GpuResource> resource = new ResourceHandle<GpuResource>(handle, loadedFuture);
            scheduleCallback(callback, resource, callbackDispatcher, loadedFuture);

            gpuReadyFutures.put(id, loadedFuture);
            return resource;
        } else {//case 4: cpu side already loaded.
            Handle<GpuResource> handle = createHandleToResource(null, LoadState.IN_PROGRESS);
            CompletableFuture<Handle<GpuResource>> future = new CompletableFuture<>();
            ResourceHandle<GpuResource> resource =  new ResourceHandle<>(handle, future);

            this.dispatcher.post(() -> {
                GpuResource gpuResource = createGpuResouce(id, cpuReadyFuture.join());
                ResourceSlot<GpuResource> slot = gpuResources.getResourceSlot(handle);
                slot.resolve(gpuResource);
                future.complete(handle);
                scheduleCallback(callback, resource, callbackDispatcher, null);
            });

            //log that resource exists
            handles.put(id, handle);
            gpuReadyFutures.put(id, future);
            return resource;
        }
    }
    private void scheduleCallback(Consumer<ResourceHandle<GpuResource>> callback, ResourceHandle<GpuResource> data, Dispatcher dispatcher, CompletableFuture future) {
        if (callback != null && dispatcher != null) {
            if (future == null) {
                dispatcher.post(() -> {
                    callback.accept(data);
                });
            } else {
                future.thenRunAsync(() -> {
                        callback.accept(data);
                }, dispatcher::post);
            }
        }
    }

    private GpuResource createGpuResouce(AssetID id, CpuAssetData data) {
        return switch (data) {
            case MeshData meshData -> uploadMesh(id, meshData);
            case TextureData textureData -> uploadTexture(id, textureData);
            case MaterialData materialData ->
                throw new UnsupportedOperationException("Material upload not implemented yet.");
            case AnimationData animationData ->
                throw new UnsupportedOperationException("Animation upload not implemented yet.");
        };
    }

    public void releaseHandle(AssetID id) {
       Handle<GpuResource> target = handles.get(id);
       gpuResources.releaseHandle(target);
    }

    public void releaseHandle(Handle<GpuResource> handle) {
        gpuResources.releaseHandle(handle);
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

    public int resouceCount() {
        return gpuResources.size();
    }

    public String toString() {
        return "";
    }

}
