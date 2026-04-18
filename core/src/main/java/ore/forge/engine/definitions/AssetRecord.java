package ore.forge.engine.definitions;


public class AssetRecord {
    private AssetGUID guid; //global unique identifier that is used to reference asset in the engine.
    private AssetType assetType; //Type of the asset (mesh, material, texture, animation)
    private String logicalName; // [CONTAINER_NAME.DISPLAY_NAME]
    private String displayName;
    private String sourcePath; //FilePath to the Object
    private int importVersion;
    private int[] dependencyIndices; //indices to other assets that this mesh depends on //indices to other assets that this mesh depends on.

    public AssetGUID guid() {
        return guid;
    }

    public void setGuid(AssetGUID guid) {
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

    public void setDependencyIndices(int[] dependencyIndices) {
        this.dependencyIndices = dependencyIndices;
    }



}
