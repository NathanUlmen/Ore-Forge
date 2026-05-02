package ore.forge.engine.importing;

import ore.forge.engine.AssetData;

public record AssetCandidate(AssetSourceKey sourceKey, AssetData assetData, AssetArtifact artifact) {
    public AssetCandidate(AssetSourceKey sourceKey, AssetData assetData) {
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
