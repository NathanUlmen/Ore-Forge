package ore.forge.serialization;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import ore.forge.engine.*;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.importing.*;
import ore.forge.engine.render.AssetHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class ImporterTest {

    @TempDir
    Path tmpDir;


    @Test
    void testImport() {
        AssetRegistry registry = new AssetRegistry(tmpDir.toString());
        AssetImporter importer = new AssetImporter(registry);
        Path sourceModel = modelFixture("Cube.gltf");
        AssetSourceKey sourceKey = new AssetSourceKey();
        sourceKey.setAssetType(AssetType.MESH);
        sourceKey.setLogicalName("cube");
        sourceKey.setAssetName("Cube");
        sourceKey.setSourcePath(sourceModel.toString());
        sourceKey.setImportVersion(1);

        importer.importGlbFile(sourceModel);

        Path output = tmpDir.resolve("temp.json");
        registry.save(output.toFile());

        AssetArtifact importedArtifact = registry.lookUp(sourceKey);
        assertNotNull(importedArtifact);
        assertEquals(sourceKey, importedArtifact.sourceKey());
        assertTrue(importedArtifact.filepath().startsWith(tmpDir));
        assertTrue(importedArtifact.filepath().getFileName().toString().endsWith(".meshbin"));

        MeshData importedMesh = new MeshDataSerializer().readObject(importedArtifact.filepath());
        assertNotNull(importedMesh);
        assertInstanceOf(MeshData.class, importedMesh);
        assertTrue(importedMesh.vbo().length > 0);
        assertTrue(importedMesh.ibo().length > 0);
    }

    @Test
    void testRegistryLoadSaveLoad() throws URISyntaxException {
        // Load registry from test resource
        AssetRegistry registry = new AssetRegistry();
        initRegistry(registry);

        // Save it to a temp file
        Path output = tmpDir.resolve("savedRegistry.json");
        registry.save(output.toFile());

        assertTrue(Files.exists(output));

        // Load saved registry again
        AssetRegistry loadedRegistry = new AssetRegistry();
        JsonReader reader = new JsonReader();
        loadedRegistry.load(reader.parse(new FileHandle(output.toFile())));

        // Verify round-trip result
        assertEquals(registry, loadedRegistry);
    }

    private void initRegistry(AssetRegistry registry) throws URISyntaxException {
        Path resourcePath = Paths.get(
            Objects.requireNonNull(
                getClass().getClassLoader().getResource("registry/basicRegistry.json")
            ).toURI()
        );

        JsonReader reader = new JsonReader();
        registry.load(reader.parse(new FileHandle(resourcePath.toFile())));
    }

    @Test
    void testSerialization() throws URISyntaxException {
        TestRegistry registry = new TestRegistry();
        AssetImporter importer = new AssetImporter(registry);
        importer.importGlbFile(modelFixture("Cube.gltf"));
        GpuResourceManager resourceManager = new GpuResourceManager(registry);


        for (AssetID id : registry.getIds()) {
            assertNotNull(resourceManager.retrieveData(id));
        }
    }

    @Test
    void testTextureImport() throws IOException {
        AssetRegistry registry = new AssetRegistry(tmpDir.toString());
        AssetImporter importer = new AssetImporter(registry);
        GpuResourceManager resourceManager = new GpuResourceManager(registry);
        importer.importGlbFile(modelFixture("texture_test.glb"));

        byte[] pngBytes = Files.readAllBytes(modelFixture("test_tex01.png"));
        AssetData data = null;

        for (AssetID id : registry.getIDs()) {
            if (registry.lookUp(id).sourceKey().assetType() == AssetType.TEXTURE) {
                System.out.println(registry.lookUp(id));
                data = resourceManager.retrieveData(id);
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

    private static class TestRegistry extends AssetRegistry {

        public Iterable<AssetID> getIds() {
            return idLookup.values();
        }

    }

}
