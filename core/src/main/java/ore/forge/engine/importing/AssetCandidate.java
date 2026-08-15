package ore.forge.engine.importing;

import ore.forge.engine.CpuAssetData;

public record AssetCandidate(AssetSourceKey sourceKey, CpuAssetData assetData, AssetArtifact artifact) {
    public AssetCandidate(AssetSourceKey sourceKey, CpuAssetData assetData) {
        this(sourceKey, assetData, null);
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
