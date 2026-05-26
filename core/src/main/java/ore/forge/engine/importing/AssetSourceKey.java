package ore.forge.engine.importing;


import ore.forge.engine.definitions.AssetType;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Nathan Ulmen
 * <p>
 * An {@link AssetSourceKey} is used to map to an {@link AssetID}. It should not be used in any gameplay systems and is
 * only used at the beginning of the import pipeline
 */
public class AssetSourceKey {
    private AssetType assetType; //Type of the asset (mesh, material, texture, animation)
    private String logicalName; // [CONTAINER_NAME]
    private String assetName; // [AssetName]
    private String sourcePath; //FilePath to the Object
    private int importVersion;


    public AssetSourceKey() {
    }

    public AssetSourceKey(AssetType assetType,
                          String logicalName,
                          String displayName,
                          String sourcePath,
                          int importVersion,
                          int[] dependencyIndices) {
        this.assetType = assetType;
        this.logicalName = logicalName;
        this.assetName = displayName;
        this.sourcePath = sourcePath;
        this.importVersion = importVersion;
    }

    /**
     *
     * @return The type of the asset that this source key corresponds to.
     */
    public AssetType assetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    /**
     * @return the name of the container that this asset came from.
     *
     */
    public String logicalName() {
        return logicalName;
    }

    public void setLogicalName(String logicalName) {
        this.logicalName = logicalName;
    }

    public String displayName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
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
            && Objects.equals(assetName, other.assetName)
            && Objects.equals(sourcePath, other.sourcePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetType, logicalName, assetName, sourcePath, importVersion);
    }

    @Override
    public String toString() {
        return "AssetSourceKey{" +
            "assetType=" + assetType +
            ", logicalName='" + logicalName + '\'' +
            ", displayName='" + assetName + '\'' +
            ", sourcePath='" + sourcePath + '\'' +
            ", importVersion=" + importVersion +
            '}';
    }
}
