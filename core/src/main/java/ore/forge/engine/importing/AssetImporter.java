package ore.forge.engine.importing;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import ore.forge.engine.AssetExtractor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class AssetImporter {
    protected static final int IMPORT_VERSION = 1;
    protected final AssetRegistry registry;

    public AssetImporter(AssetRegistry registry) {
        this.registry = registry;
    }

    public void importGlbFile(Path file) {
        GltfModel contents = loadGlbFile(file);
        AssetExtractor.extractAssets(contents, file, registry);
    }


    private GltfModel loadGlbFile(Path file) {
        try {
            return new GltfModelReader().read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
