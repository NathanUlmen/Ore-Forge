package ore.forge;

import de.javagl.jgltf.model.GltfModel;
import ore.forge.engine.AssetLoader;
import ore.forge.engine.definitions.AssetRecord;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshData;
import ore.forge.engine.definitions.MeshDataSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.TextParsingException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetLoaderTest {
    static int NUM_THREADS = 1;

    @TempDir
    static Path tmpDir;

    @Test
    void loadDirectoryLoadsModelsFromTestResources() throws URISyntaxException {
        RecordingAssetLoader loader = new RecordingAssetLoader(NUM_THREADS);
        Path modelsDir = Path.of(getClass().getResource("/models").toURI());

        loader.loadDirectory(modelsDir, tmpDir);
        loader.end(null);

        assertEquals(
            Set.of("Cube.gltf", "Emerald.gltf", "Sphere.gltf", "Wedge.gltf"),
            Set.copyOf(loader.loadedFileNames())
        );
        for (GltfModel model : loader.loadedModels) {
            assertNotNull(model);
            assertNotNull(model.getAssetModel());
            assertFalse(model.getMeshModels().isEmpty());
        }
        assertEquals(4, loader.getAssetRecords().size());
    }

    @Test
    void loadWriteRead() throws IOException, URISyntaxException {
        RecordingAssetLoader loader = new RecordingAssetLoader(NUM_THREADS);
        Path modelsDir = Path.of(getClass().getResource("/models").toURI());
        MeshDataSerializer serializer = new MeshDataSerializer();
        List<Path> producedFiles;

        loader.loadDirectory(modelsDir, tmpDir);
        loader.end(Path.of("temp"));

        try (var files = Files.list(tmpDir)) {
            producedFiles = files
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".bin"))
                .toList();
        }

        assertEquals(loader.getAssetRecords().size(), producedFiles.size());

        try {
            for (Path producedFile : producedFiles) {
                assertTrue(Files.size(producedFile) > 0, "Produced file should not be empty: " + producedFile); //
                MeshData meshData = serializer.readObject(producedFile.toString());
                assertNotNull(meshData);
                assertTrue(meshData.vbo().remaining() > 0, "Produced mesh should contain vertex data");
            }
        } finally {
            for (Path producedFile : producedFiles) {
                Files.deleteIfExists(producedFile);
                assertFalse(Files.exists(producedFile), "Produced file should be deleted after verification: " + producedFile);
            }
        }
    }


    @Test
    void testLoadRegistry() {
        RecordingAssetLoader loader = new RecordingAssetLoader(NUM_THREADS);
        loader.loadRegistry(Path.of(getClass().getResource("/registry/basicRegistry.json").getFile()).toFile());

        Set<AssetRecord> expected = Set.of(
            new AssetRecord(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                AssetType.MESH,
                "Environment.TreeOak_A",
                "TreeOak_A",
                "assets/models/environment/tree_oak_a.glb",
                3,
                new int[]{1, 2}
            ),
            new AssetRecord(
                UUID.fromString("8c4b2a9e-6f42-4d8d-9a1d-3f2d95c8f101"),
                AssetType.MATERIAL,
                "Environment.TreeOak_A_Mat",
                "TreeOak_A_Mat",
                "assets/materials/tree_oak_a.mat",
                1,
                new int[]{2}
            ),
            new AssetRecord(
                UUID.fromString("2d9c7f61-9f8d-45b7-bb8c-8ab7f7d3c202"),
                AssetType.TEXTURE,
                "Environment.TreeOak_A_Albedo",
                "TreeOak_A_Albedo",
                "assets/textures/tree_oak_a_albedo.png",
                1,
                new int[]{}
            )
        );

        for (AssetRecord record : loader.getAssetRecords()) {
            if (!expected.contains(record)) {
                System.out.println("Not in expected: " + record);
            }
        }

        assertTrue(expected.containsAll(loader.getAssetRecords()));
    }

    private static final class RecordingAssetLoader extends AssetLoader {
        private final List<String> loadedFiles = new ArrayList<>();
        private final List<GltfModel> loadedModels = new ArrayList<>();

        private RecordingAssetLoader(int numThreads) {
            super(numThreads);
        }

        @Override
        public AssetLoader.ExtractedAssetData extractAssetData(GltfModel gltfModel, Path sceneFilePath) {
            synchronized (this) {
                loadedFiles.add(sceneFilePath.getFileName().toString());
                loadedModels.add(gltfModel);
            }
            return super.extractAssetData(gltfModel, sceneFilePath);
        }

        private List<String> loadedFileNames() {
            return loadedFiles;
        }

        public List<AssetRecord> getAssetRecords() {
            return this.registry;
        }
    }

}
