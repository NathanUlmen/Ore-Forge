package ore.forge.engine.resources;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.Handle;
import ore.forge.engine.definitions.AssetType;

import java.nio.file.Path;

/**
 * Public resource-system entry point for importing, registry persistence, CPU residency, and GPU residency.
 */
public class ResourceManager {
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
        this.assetManager = new AssetManager(registry);
        this.gpuResourceManager = new GpuResourceManager(assetManager);
    }

    public void importGltf(Path file) {
        importer.importGlbFile(file);
    }

    public CpuAssetData getCpuAsset(AssetID id) {
        return assetManager.getCpuAsset(id);
    }

    public Handle<GpuResource> getGpuHandle(AssetID id) {
        return gpuResourceManager.accquireHandle(id);
    }

    public GpuResource getGpuResource(Handle<GpuResource> assetHandle) {
        return gpuResourceManager.getGpuResource(assetHandle);
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

    public void accquireGpuResource(AssetID id) {
        gpuResourceManager.accquireHandle(id);
    }

    public void releaseGpuResource(AssetID id) {
        gpuResourceManager.releaseHandle(id);
    }

    public void releaseGpuResource(Handle<GpuResource> handle) {
        gpuResourceManager.releaseHandle(handle);
    }

    public int activeGpuResources() {
        return gpuResourceManager.resouceCount();
    }

}
