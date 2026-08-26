package ore.forge.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.*;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.resources.AssetID;
import ore.forge.engine.resources.CpuAssetData;
import ore.forge.engine.resources.MeshData;
import ore.forge.engine.resources.ResourceManager;
import ore.forge.engine.resources.TextureData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ImporterTest {

    @TempDir
    Path tmpDir;


    @Test
    void testImport() {
        ResourceManager resourceManager = new ResourceManager(tmpDir.toString());
        Path sourceModel = modelFixture("Cube.gltf");
        resourceManager.importGltf(sourceModel);

        Path output = tmpDir.resolve("temp.json");
        resourceManager.saveRegistry(output);

        JsonValue registryJson = new JsonReader().parse(new FileHandle(output.toFile()));
        assertTrue(registryJson.size > 0);

        JsonValue meshEntry = findEntryByAssetType(registryJson, "MESH");
        assertNotNull(meshEntry);

        Path bakedMeshPath = Path.of(meshEntry.get("artifact").getString("filePath"));
        assertTrue(bakedMeshPath.startsWith(tmpDir));
        assertTrue(bakedMeshPath.getFileName().toString().endsWith(".meshbin"));

        MeshData importedMesh = new MeshDataSerializer().readObject(bakedMeshPath);
        assertTrue(importedMesh.vbo().length > 0);
        assertTrue(importedMesh.ibo().length > 0);
    }

    @Test
    void testRegistryLoadSaveLoad() throws URISyntaxException {
        ResourceManager resourceManager = new ResourceManager();
        initRegistry(resourceManager);
        Path output = tmpDir.resolve("savedRegistry.json");
        resourceManager.saveRegistry(output);

        assertTrue(Files.exists(output));

        ResourceManager loadedResourceManager = new ResourceManager();
        loadedResourceManager.loadRegistry(new FileHandle(output.toFile()));

        assertEquals(assetIds(resourceManager), assetIds(loadedResourceManager));
        assertEquals(assetTypes(resourceManager), assetTypes(loadedResourceManager));
    }

    private void initRegistry(ResourceManager resourceManager) throws URISyntaxException {
        Path resourcePath = Paths.get(
            Objects.requireNonNull(
                getClass().getClassLoader().getResource("registry/basicRegistry.json")
            ).toURI()
        );

        JsonReader reader = new JsonReader();
        resourceManager.loadRegistry(reader.parse(new FileHandle(resourcePath.toFile())));
    }

    @Test
    void testSerialization() {
        ResourceManager resourceManager = new ResourceManager();
        resourceManager.importGltf(modelFixture("Cube.gltf"));

        for (AssetID id : resourceManager.getAssetIDs()) {
            assertNotNull(resourceManager.getCpuAsset(id));
        }
    }

    @Test
    void testTextureImport() throws IOException {
        ResourceManager resourceManager = new ResourceManager(tmpDir.toString());
        resourceManager.importGltf(modelFixture("texture_test.glb"));

        byte[] pngBytes = Files.readAllBytes(modelFixture("test_tex01.png"));
        CpuAssetData data = null;

        for (AssetID id : resourceManager.getAssetIDs()) {
            if (resourceManager.getAssetType(id) == AssetType.TEXTURE) {
                data = resourceManager.getCpuAsset(id);
            }
        }

        if (data instanceof TextureData textureData) {
            assertArrayEquals(pngBytes, textureData.encodedBytes());
        }
    }


    private Path modelFixture(String fileName) {
        try {
            return Path.of(getClass().getResource("/models/" + fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve fixture: " + fileName, e);
        }
    }

    private static JsonValue findEntryByAssetType(JsonValue registryJson, String assetType) {
        for (JsonValue entry : registryJson) {
            if (assetType.equals(entry.get("artifact").get("sourceKey").getString("assetType"))) {
                return entry;
            }
        }
        return null;
    }

    private static Set<String> assetIds(ResourceManager resourceManager) {
        Set<String> ids = new HashSet<>();
        for (AssetID id : resourceManager.getAssetIDs()) {
            ids.add(id.toString());
        }
        return ids;
    }

    private static List<AssetType> assetTypes(ResourceManager resourceManager) {
        List<AssetType> types = new ArrayList<>();
        for (AssetID id : resourceManager.getAssetIDs()) {
            types.add(resourceManager.getAssetType(id));
        }
        return types;
    }

}
