package ore.forge;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import ore.forge.engine.MeshData;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.importing.AssetArtifact;
import ore.forge.engine.importing.AssetImporter;
import ore.forge.engine.importing.AssetRegistry;
import ore.forge.engine.importing.AssetSourceKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImporterTest {

    @TempDir
    Path tmpDir;


    @Test
    void testImport() throws IOException {
        AssetRegistry registry = new AssetRegistry();
        AssetImporter importer = new AssetImporter(registry, tmpDir);
        Path sourceModel = modelFixture("Cube.gltf");
        AssetSourceKey sourceKey = new AssetSourceKey();
        sourceKey.setAssetType(AssetType.MESH);
        sourceKey.setContainerName("cube");
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
        assertTrue(importedArtifact.filepath().getFileName().toString().endsWith(".bin"));

        MeshData importedMesh = new MeshDataSerializer().readObject(importedArtifact.filepath());
        assertNotNull(importedMesh);
        assertInstanceOf(MeshData.class, importedMesh);
        assertTrue(importedMesh.vbo().remaining() > 0);
        assertTrue(importedMesh.ebo().remaining() > 0);
    }

    @Test
    void testRegistryLoadSaveLoad() throws URISyntaxException {
        // Load registry from test resource
        Path resourcePath = Paths.get(
            Objects.requireNonNull(
                getClass().getClassLoader().getResource("registry/basicRegistry.json")
            ).toURI()
        );

        AssetRegistry registry = new AssetRegistry();
        JsonReader reader = new JsonReader();
        registry.load(reader.parse(new FileHandle(resourcePath.toFile())));

        // Save it to a temp file
        Path output = tmpDir.resolve("savedRegistry.json");
        registry.save(output.toFile());

        assertTrue(Files.exists(output));

        // Load saved registry again
        AssetRegistry loadedRegistry = new AssetRegistry();
        loadedRegistry.load(reader.parse(new FileHandle(output.toFile())));

        // Verify round-trip result
        assertEquals(registry, loadedRegistry);
    }

    private Path modelFixture(String fileName) {
        try {
            return Path.of(getClass().getResource("/models/" + fileName).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to resolve fixture: " + fileName, e);
        }
    }
}
