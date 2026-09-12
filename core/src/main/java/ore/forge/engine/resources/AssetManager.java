package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ore.forge.engine.Handle;
import ore.forge.engine.HandleRegistry;
import ore.forge.engine.Dispatcher;
import ore.forge.engine.VertexAttribute;
import ore.forge.engine.resources.ResourceSlot.LoadState;

final class AssetManager {
    private static final String LOG_TAG = AssetManager.class.getName();
    // Native-free 1x1 PNG placeholder so CPU-only tests do not require libGDX image natives at class load time.
    private static final byte[] DEFAULT_TEXTURE_BYTES = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR42mP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
    );
    private static final MeshData DEFAULT_MESH = createDefaultMesh();
    private static final TextureData DEFAULT_TEXTURE = createDefaultTexture();
    private final HashMap<AssetID, Handle<CpuAssetData>> handleLookup;
    private final HashMap<AssetID, CompletableFuture<CpuAssetData>> cpuReadyFutures;
    private final HandleRegistry<CpuAssetData> handleRegistry;
    private final AssetRegistry assetRegistry;
    private final AssetDataSerializer serializer;
    private final Dispatcher dispatcher;

    public AssetManager(AssetRegistry registry, Dispatcher dispatcher) {
        this.cpuReadyFutures = new HashMap<>();
        this.assetRegistry = registry;
        this.handleLookup = new HashMap<>();
        this.handleRegistry = new HandleRegistry<>();
        this.serializer = new AssetDataSerializer();
        this.dispatcher = dispatcher;
    }

    public ResourceHandle<CpuAssetData> acquireResourceHandle(AssetID id, Consumer<ResourceHandle<CpuAssetData>> callback, Dispatcher dispatcher) {
        Handle<CpuAssetData> lookupHandle = handleLookup.get(id);
        if (lookupHandle != null) { //case 1: target is already loaded or is in flight.
            return new ResourceHandle<>(handleRegistry.accquireHandle(lookupHandle), this.getCpuReadyFuture(id));
        }

        //case 2: load has not been requested
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


        ResourceSlot<CpuAssetData> slot = handleRegistry.getResourceSlot(handle);
        CompletableFuture<CpuAssetData> loadFuture = serializer.load(target, slot);

        CompletableFuture<CpuAssetData> cpuReady = new CompletableFuture<>();
        cpuReadyFutures.put(id, cpuReady);

        loadFuture.thenAcceptAsync(loadedData -> {
            resolveLoad(handle, id, cpuReady, slot, loadedData);
        }, dispatcher::post);

        if (target.dependencies() != null){
            for (ore.forge.engine.resources.AssetArtifact dependency : target.dependencies()) {
                acquireResourceHandle(dependency.assetID(), null, null);
            }
        }

        return new ResourceHandle<>(handle, cpuReady);
    }

    private void resolveLoad(Handle<CpuAssetData> handle, AssetID id, CompletableFuture<CpuAssetData> cpuReady, ResourceSlot<CpuAssetData> slot, CpuAssetData result) {
        if (slot != null) {
            slot.resolve(result);
            slot.setLoadState(LoadState.COMPLETED);
            cpuReady.complete(result);
        } else {
            result.dispose();
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

    public ResourceSlot<CpuAssetData> getSlot(Handle<CpuAssetData> handle) {
        return handleRegistry.getResourceSlot(handle);
    }

    public CpuAssetData resolvePlaceHolder(AssetArtifact target) {
        return switch (target.type()) {
            case MESH -> DEFAULT_MESH;
            case TEXTURE -> DEFAULT_TEXTURE;
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
