package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import ore.forge.engine.Handle;
import ore.forge.engine.HandleRegistry;
import ore.forge.engine.render.Renderer;

import java.util.HashMap;

/**
 * @author Nathan Ulmen
 * Acts as an interface that the {@link Renderer} and other systems can interact with to reference Assets.
 * It ensures uniqueness among assets, preventing multiple instances of the same Asset.
 * It handles the loading of asset dependencies.
 *
 *
 */
final class GpuResourceManager {
    private final AssetManager assetManager;
    private final HashMap<AssetID, Handle<GpuResource>> handles;
    private final HandleRegistry<GpuResource> gpuResources;

    public GpuResourceManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.handles = new HashMap<>();
        this.gpuResources = new HandleRegistry<>();
    }

    /**
     * Given an {@link AssetID} the {@link GpuResourceManager} will return a handle to the
     * referenced asset. If the referenced asset is not currently in memory it will be loaded from disk
     * and then uploaded to the GPU.
     *
     * @param id to an asset that want a handle to.
     * @return A handle to the asset that the id references.
     */
    public Handle<GpuResource> getHandle(AssetID id) {
        Handle<GpuResource> existingHandle = handles.get(id);
        if (existingHandle != null) {
            return existingHandle;
        }
        CpuAssetData data = assetManager.getCpuAsset(id);
        return switch (data) {
            case MeshData meshData -> uploadMesh(id, meshData);
            case TextureData textureData -> uploadTexture(id, textureData);
            case MaterialData materialData ->
                throw new UnsupportedOperationException("Material upload not implemented yet.");
            case AnimationData animationData ->
                throw new UnsupportedOperationException("Animation upload not implemented yet.");
        };
    }

    /**
     *
     */
    private Handle<GpuResource> uploadMesh(AssetID id, MeshData meshData) {
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

        Handle<GpuResource> handle = gpuResources.addResource(meshResource);
        handles.put(id, handle);


        return handle;
    }

    /**
     * Uploads {@link Pixmap} data to the GPU to be used as a texture. If the
     * {@link Pixmap} has not been constructed from the encoded bytes yet, that operation will
     * be performed.
     *
     * @param id to be mapped to the {@link TextureHandle}.
     * @return TextureHandle that points to the {@link GpuTextureResource}
     */
    private Handle<GpuResource> uploadTexture(AssetID id, TextureData textureData) {
        Pixmap map = textureData.pixmap();
        GpuTextureResource textureResource = new GpuTextureResource(map);
        Handle<GpuResource> handle = gpuResources.addResource(textureResource);
        handles.put(id, handle);

        return handle;
    }

    /**
     * Used to reference resources stored on the GPU
     *
     * @param assetHandle Handle to the resource on that you want to reference on that's stored on the GPU.
     * @return resource that the assetHandle references.
     */
    public GpuResource getGpuResource(Handle<GpuResource> assetHandle) {
        return gpuResources.getResource(assetHandle);
    }

    public String toString() {
        return "";
    }

}
