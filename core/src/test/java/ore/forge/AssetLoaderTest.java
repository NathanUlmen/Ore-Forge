package ore.forge;

import de.javagl.jgltf.model.GltfModel;
import ore.forge.engine.AssetLoader;
import ore.forge.engine.definitions.AssetRecord;
import ore.forge.engine.definitions.MeshData;
import ore.forge.engine.definitions.MeshDataSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        loader.end();

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
        loader.end();

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
