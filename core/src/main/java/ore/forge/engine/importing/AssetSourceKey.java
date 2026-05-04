package ore.forge.engine.importing;


import ore.forge.engine.definitions.AssetType;

import java.util.Objects;
import java.util.UUID;

public class AssetSourceKey {
    private AssetType assetType; //Type of the asset (mesh, material, texture, animation)
    private String logicalName; // [CONTAINER_NAME]
    private String AssetName; // [AssetName]
    private String sourcePath; //FilePath to the Object
    private int importVersion;


    public AssetSourceKey() {

    }

    public AssetSourceKey(UUID guid,
                          AssetType assetType,
                          String logicalName,
                          String displayName,
                          String sourcePath,
                          int importVersion,
                          int[] dependencyIndices) {
        this.assetType = assetType;
        this.logicalName = logicalName;
        this.AssetName = displayName;
        this.sourcePath = sourcePath;
        this.importVersion = importVersion;
    }

    public AssetType assetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    public String logicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String displayName() {
        return AssetName;
    }

    public void setAssetName(String assetName) {
        this.AssetName = assetName;
    }

    public String sourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public int importVersion() {
        return importVersion;
    }

    public void setImportVersion(int importVersion) {
        this.importVersion = importVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AssetSourceKey other)) {
            return false;
        }
        return importVersion == other.importVersion
            && assetType == other.assetType
            && Objects.equals(logicalName, other.logicalName)
            && Objects.equals(AssetName, other.AssetName)
            && Objects.equals(sourcePath, other.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetType, logicalName, AssetName, sourcePath, importVersion);
    }

    @Override
    public String toString() {
        return "AssetSourceKey{" +
            "assetType=" + assetType +
            ", logicalName='" + logicalName + '\'' +
            ", displayName='" + AssetName + '\'' +
            ", sourcePath='" + sourcePath + '\'' +
            ", importVersion=" + importVersion +
            '}';
    }
}
