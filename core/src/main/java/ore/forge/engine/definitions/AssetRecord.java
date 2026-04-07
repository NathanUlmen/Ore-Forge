package ore.forge.engine.definitions;


public record AssetRecord(
    AssetGUID guid,
    AssetType asset,
    String logicalName,
    String displayName,
    String sourcePath,
    long contentHash,
    int importVersion,
    int[] dependencyIndices
) {

}
