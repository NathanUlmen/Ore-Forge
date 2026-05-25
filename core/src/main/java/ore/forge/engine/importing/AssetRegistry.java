package ore.forge.engine.importing;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Nathan Ulmen
 * <p>
 * An AssetRegisty enables the engine to Map references/ids
 * to assets that are stored on disk.
 * <p>
 * The registry is populated using via JSON.
 *
 */
public class AssetRegistry {
    protected HashMap<AssetSourceKey, AssetID> idLookup;
    protected HashMap<AssetID, AssetArtifact> artifactLookup;
    protected final Path bakedDir;

    /**
     * @param bakedOutputDir directory registry will save to.
     *
     */
    public AssetRegistry(String bakedOutputDir) {
        bakedDir = Path.of(bakedOutputDir);
        artifactLookup = new HashMap<>();
        idLookup = new HashMap<>();
    }

    public AssetRegistry() {
        this("assets");
    }

    /**
     * Creates a new entry in the registry if it's not currently present.
     *
     * @param candidate - Asset candidate that might not already be present in the registry.
     * @return true if new entry was created, false if already present.
     */
    public boolean createNewEntry(AssetCandidate candidate) {
        //Case 1: source Key already mapped to a UUID: do nothing
        if (idLookup.get(candidate.sourceKey()) != null) {
            return false;
        }

        //Case 2: completely new asset
        AssetID id = new AssetID(UUID.randomUUID());
        idLookup.put(candidate.sourceKey(), id);
        if (candidate.artifact() != null) {
            candidate.artifact().setAssetID(id);
            artifactLookup.put(id, candidate.artifact());
            assert candidate.artifact().sourceKey().equals(candidate.sourceKey());
            return true;
        }
        throw new IllegalStateException();
    }

    public AssetArtifact lookUp(AssetSourceKey sourceKey) {
        AssetID key = idLookup.get(sourceKey);
        return artifactLookup.get(key);
    }

    /**
     *
     * @param id a reference to an asset.
     * @return an {@link AssetArtifact} that the corresponding id references.
     */
    public AssetArtifact lookUp(AssetID id) {
        return artifactLookup.get(id);
    }

    /**
     * @return {@link Iterable}of all {@link AssetID}'s stored in the registry.
     */
    public Iterable<AssetID> getIDs() {
        return idLookup.values();
    }

    /**
     * Extracts {@link AssetRegistryData} from a {@link JsonValue} and appends it to the {@link AssetRegistry}.
     * This operation doesn't overwrite existing values, but does add/append to the data currently stored in the {@link AssetRegistry}.
     *
     * @param jsonValue JSON data comprised of {@link AssetRegistryData} to be loaded into the {@link AssetRegistry}
     *
     */
    public void load(JsonValue jsonValue) {
        Json json = getJson();
        for (JsonValue value : jsonValue) {
            AssetRegistryData data = json.readValue(AssetRegistryData.class, value);
            AssetID identifier = new AssetID(data.uuid);
            artifactLookup.put(identifier, data.artifact);
            idLookup.put(data.artifact.sourceKey(), identifier);
        }
    }

    /**
     * Saves the {@link AssetRegistry} and all data to disk.
     * Does not mutate it in any way.
     *
     * @param outputFile Location the registry data will be written to.
     */
    public void save(File outputFile) {
        ArrayList<AssetRegistryData> data = new ArrayList<>();
        for (Map.Entry<AssetID, AssetArtifact> entry : artifactLookup.entrySet()) {
            AssetRegistryData dataItem = new AssetRegistryData();
            dataItem.uuid = entry.getKey().toString();
            dataItem.artifact = entry.getValue();
            data.add(dataItem);
        }
        Json json = this.getJson();
        new FileHandle(outputFile).writeString(json.prettyPrint(data), false);
    }

    private Json getJson() {
        Json json = new Json();
        json.setSerializer(AssetArtifact.class, new AssetArtifactSerializer());
        json.setOutputType(JsonWriter.OutputType.json);
        return json;
    }

    public Path getBakedDir() {
        return bakedDir;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssetRegistry other) {
            return artifactLookup.equals(other.artifactLookup) && idLookup.equals(other.idLookup);
        }
        return false;
    }

    @Override
    public String toString() {
        return "AssetRegistry{" +
            "idLookup=" + idLookup +
            ", artifactLookup=" + artifactLookup +
            '}';
    }

    private static class AssetRegistryData {
        private String uuid;
        private AssetArtifact artifact;
    }

    private static class AssetArtifactSerializer implements Json.Serializer<AssetArtifact> {
        @Override
        public void write(Json json, AssetArtifact object, Class knownType) {
            json.writeObjectStart();

            json.writeValue("filePath", object.filepath().toString());
            json.writeValue("sourceKey", object.sourceKey());
            json.writeValue("dependencies", object.dependencies());
            json.writeValue("uuid", object.assetID().toString());


            json.writeObjectEnd();
        }

        @Override
        public AssetArtifact read(Json json, JsonValue jsonData, Class type) {
            String filePath = jsonData.getString("filePath");
            ArrayList<AssetArtifact> dependencies = json.readValue(ArrayList.class, AssetArtifact.class, jsonData.get("dependencies"));
            AssetSourceKey sourceKey = json.readValue(AssetSourceKey.class, jsonData.get("sourceKey"));
            String uuid = jsonData.getString("uuid");
            return new AssetArtifact(filePath, dependencies, sourceKey, new AssetID(uuid));
        }
    }


}
