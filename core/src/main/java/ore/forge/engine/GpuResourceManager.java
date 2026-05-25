package ore.forge.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import ore.forge.engine.importing.AssetArtifact;
import ore.forge.engine.importing.AssetID;
import ore.forge.engine.importing.AssetRegistry;
import ore.forge.engine.profiling.Stopwatch;
import ore.forge.engine.render.*;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Nathan Ulmen
 * Acts as an interface that the {@link Renderer} and other systems can interact with to reference Assets.
 * It ensures uniqueness among assets, preventing multiple instances of the same Asset.
 * It handles the loading of asset dependencies.
 *
 *
 */
public class GpuResourceManager {
    private final AssetRegistry registry;
    private final AssetDataSerializer serializer;
    private final HashMap<AssetID, AssetData> lookup;
    private final HashMap<AssetID, AssetHandle> handles;
    private final HashMap<AssetHandle, GpuResource> gpuResources;


    public GpuResourceManager(AssetRegistry registry) {
        this.registry = registry;
        lookup = new HashMap<>();
        handles = new HashMap<>();
        serializer = new AssetDataSerializer();
        gpuResources = new HashMap<>();
    }

    /**
     * Given an {@link AssetID} the {@link GpuResourceManager} will return a handle to the
     * referenced asset. If the referenced asset is not currently in memory it will be loaded from disk
     * and then uploaded to the GPU.
     *
     * @param id to an asset that want a handle to.
     * @return A handle to the asset that the id references.
     */
    public AssetHandle getHandle(AssetID id) {
        AssetHandle existingHandle = handles.get(id);
        if (existingHandle != null) {
            return existingHandle;
        }

        AssetData data = retrieveData(id);

        return switch (data) {
            case MeshData meshData -> uploadMesh(id, meshData);
            case TextureData textureData ->
                throw new UnsupportedOperationException("Texture upload not implemented yet.");
            case MaterialData materialData ->
                throw new UnsupportedOperationException("Material upload not implemented yet.");
            case AnimationData animationData ->
                throw new UnsupportedOperationException("Animation upload not implemented yet.");
        };
    }

    /**
     *
     */
    private MeshHandle uploadMesh(AssetID id, MeshData meshData) {
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

        MeshHandle handle = new MeshHandle();
        handles.put(id, handle);
        gpuResources.put(handle, meshResource);

        return handle;
    }

    /**
     * Used to reference resources stored on the GPU
     *
     * @param assetHandle - Handle to the resource on that you want to reference on that's stored on the GPU.
     * @return resource that the assetHandle references.
     */
    public GpuResource getGpuResource(AssetHandle assetHandle) {
        assert gpuResources.get(assetHandle) != null : "AssetHandle: " + assetHandle + "\t Has not been uploaded to the GPU";
        return gpuResources.get(assetHandle);
    }

    /**
     * Used to retrieve AssetData that is stored on CPU. If the data is not present
     * it will be loaded from disk into memory.
     *
     * @param reference to the AssetData
     * @return AssetData that the {@link AssetID} points to.
     *
     */
    public AssetData retrieveData(AssetID reference) {
        AssetData assetData = lookup.get(reference);
        if (assetData != null) {
            return assetData;
        }

        AssetArtifact target = registry.lookUp(reference);
        assert target != null : "AssetID: " + reference + ", failed to map to an AssetArtifact.";

        assetData = serializer.load(target);
        lookup.put(reference, assetData);

        //load dependencies
        if (target.dependencies() != null) {
            for (AssetArtifact dependency : target.dependencies()) {
                retrieveData(dependency.assetID());
            }
        }

        return assetData;
    }

    public String toString() {
        return "";
    }

}
