package ore.forge.engine.resources;

import java.util.HashMap;

final class AssetManager {
    private final HashMap<AssetID, CpuAssetData> lookup;
    private final AssetRegistry assetRegistry;
    private final AssetDataSerializer serializer;

    public AssetManager(AssetRegistry registry) {
        this.assetRegistry = registry;
        this.lookup = new HashMap<>();
        this.serializer = new AssetDataSerializer();
    }

    public CpuAssetData getCpuAsset(AssetID id) {
        CpuAssetData cachedAsset = lookup.get(id);
        if (cachedAsset != null) {
            return cachedAsset;
        }

        AssetArtifact target = assetRegistry.lookUp(id);
        if (target == null) {
            throw new IllegalArgumentException("Target artifact of id:[" + id + "] was not present in asset registry.");
        }

        CpuAssetData assetData = serializer.load(target);
        lookup.put(id, assetData);

        if (target.dependencies() != null) {
            for (AssetArtifact dependency : target.dependencies()) {
                getCpuAsset(dependency.assetID());
            }
        }

        return assetData;
    }
}
