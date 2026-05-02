package ore.forge.engine.importing;

import com.badlogic.gdx.files.FileHandle;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author Nathan Ulmen
 *
 *
 */
public class AssetArtifact {
    private Path filePath; //path to baked asset
    private ArrayList<AssetArtifact> dependencies; //other assets this asset depends on
    private AssetSourceKey sourceKey; //Key that maps to UUID.
    private AssetID assetID;

    public AssetArtifact() {
    }

    public AssetArtifact(String filePath, ArrayList<AssetArtifact> dependencies, AssetSourceKey sourceKey, AssetID assetID) {
        this.dependencies = dependencies;
        this.filePath = Path.of(filePath);
        this.sourceKey = sourceKey;
        this.assetID = assetID;
    }

    public AssetSourceKey sourceKey() {
        return sourceKey;
    }

    public ArrayList<AssetArtifact> dependencies() {
        return dependencies;
    }

    public Path filepath() {
        return filePath;
    }

    public AssetID assetID() {
        return assetID;
    }

    public void setAssetID(AssetID assetID) {
        this.assetID = assetID;
    }

    @Override
    public String toString() {
        return "AssetArtifact{filePath=" + filePath + "\tsourceKey=" + sourceKey + "\tdependencies=" + dependencies + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetArtifact other) {
            return other.assetID.equals(this.assetID);
        }
        return false;
    }
}
