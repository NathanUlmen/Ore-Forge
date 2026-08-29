package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.VertexAttributes;

import com.badlogic.gdx.utils.GdxRuntimeException;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import ore.forge.engine.Handle;
import ore.forge.engine.HandleRegistry;
import ore.forge.engine.VertexAttribute;
import ore.forge.engine.profiling.Stopwatch;

final class AssetManager {
    private static final String LOG_TAG = AssetManager.class.getName();
    private static final MeshData DEFAULT_MESH = createDefaultMesh();
    private static final TextureData DEFAULT_TEXTURE = createDefaultTexture();
    private final HashMap<AssetID, Handle<CpuAssetData>> handleLookup;
    private final HashMap<AssetID, CompletableFuture<Handle<CpuAssetData>>> cpuReadyFutures;
    private final HandleRegistry<CpuAssetData> handleRegistry;
    private final AssetRegistry assetRegistry;
    private final AssetDataSerializer serializer;

    public AssetManager(AssetRegistry registry) {
        this.cpuReadyFutures = new HashMap<>();
        this.assetRegistry = registry;
        this.handleLookup = new HashMap<>();
        this.handleRegistry = new HandleRegistry<>();
        this.serializer = new AssetDataSerializer();
    }

    public Handle<CpuAssetData> getCpuAsset(AssetID id) {
        //Case 1: Resource already loaded
        if (handleLookup.get(id) != null) {
            return handleRegistry.accquireHandle(handleLookup.get(id));
        }

        //Case 2: resouce not loaded or in process of loading so we need to begin that process
        AssetArtifact target = assetRegistry.lookUp(id);
        if (target == null) {
            Gdx.app.error(LOG_TAG, "Target artifact of id:[" + id + "] was not present in asset registry.", new IllegalArgumentException());
        }
        
        long start = Stopwatch.timeNow(TimeUnit.MILLISECONDS);
        Handle<CpuAssetData> handle = handleRegistry.addResource(resolvePlaceHolder(target));
        ResourceSlot slot = handleRegistry.getResourceSlot(handle);
        handleLookup.put(id, handle);


        //flag that will complete when this data has resolved to a slot
        CompletableFuture<Handle<CpuAssetData>> cpuReady = new CompletableFuture<>();
        cpuReadyFutures.put(id, cpuReady);

        CompletableFuture<CpuAssetData> future = serializer.load(target);
        future.thenAccept(result -> 
            Gdx.app.postRunnable(() -> {
                if (slot != null) { //carry on as normal
                    slot.resolve(result);
                    cpuReady.complete(handle);
                    Gdx.app.log(LOG_TAG, "Resolved resource " + target + " in " + Stopwatch.elapsedString(start, TimeUnit.MILLISECONDS));
                } else {//nothing references resource anymore so we get rid of it. 
                    result.dispose();
                    cpuReady.cancel(false);
                }
                cpuReadyFutures.remove(id);
            }));

        if (target.dependencies() != null) {
            for (AssetArtifact dependency : target.dependencies()) {
                getCpuAsset(dependency.assetID());
            }
        }

        return handle;
    }


    /**
     * Will return a completable future. the future is completed if it has already resolved. if the future is still in progress will return that one instead
     * @param id target
     * @return a complete future if the value has already been resolved or an in progress one if still in the process. returns null if  
     */
    public CompletableFuture<Handle<CpuAssetData>> getCpuReadyFuture(AssetID id) {
        var handle = handleLookup.get(id);
        if (handle != null && getSlot(handle).isResolved()) { //future has already been completed and we are no longer tracking it
            return CompletableFuture.completedFuture(handle);
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
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 0f, 1f, 1f);
        pixmap.fill();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            PixmapIO.PNG writer = new PixmapIO.PNG();
            writer.write(output, pixmap);
            writer.dispose();
            return new TextureData(output.toByteArray());
        } catch (Exception e) {
            throw new GdxRuntimeException("Failed to build default placeholder texture.", e);
        } finally {
            pixmap.dispose();
        }
    }

}
