package ore.forge.engine.definitions;


import java.util.Arrays;
import java.util.UUID;

public class AssetRecord {
    private UUID guid; //global unique identifier that is used to reference asset in the engine.
    private AssetType assetType; //Type of the asset (mesh, material, texture, animation)
    private String logicalName; // [CONTAINER_NAME.DISPLAY_NAME]
    private String displayName;
    private String sourcePath; //FilePath to the Object
    private int importVersion;
    private int[] dependencyIndices; //indices to other assets that this mesh depends on //indices to other assets that this mesh depends on.


    public AssetRecord() {

    }

    public AssetRecord(UUID guid,
                       AssetType assetType,
                       String logicalName,
                       String displayName,
                       String sourcePath,
                       int importVersion,
                       int[] dependencyIndices) {
        this.guid = guid;
        this.assetType = assetType;
        this.logicalName = logicalName;
        this.displayName = displayName;
        this.sourcePath = sourcePath;
        this.importVersion = importVersion;
        this.dependencyIndices = dependencyIndices;
    }

    public UUID guid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
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
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public int[] dependencyIndices() {
        return dependencyIndices;
    }

    public void setDependencyIndices(int[] dependencyIndices) { this.dependencyIndices = dependencyIndices; }

    @Override
    public boolean equals(Object o) {
        if (o == null) {return false;}
        AssetRecord other = (AssetRecord) o;
        return other.guid.equals(this.guid);
    }

    @Override
    public int hashCode() {
        return guid.hashCode();
    }

    @Override
    public String toString() {
        return "AssetRecord{" +
            "guid=" + guid +
            ", assetType=" + assetType +
            ", logicalName='" + logicalName + '\'' +
            ", displayName='" + displayName + '\'' +
            ", sourcePath='" + sourcePath + '\'' +
            ", importVersion=" + importVersion +
            ", dependencyIndices=" + Arrays.toString(dependencyIndices) +
            '}';
    }
}
