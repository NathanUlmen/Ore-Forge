package ore.forge.engine.resources;

import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.io.GltfModelReader;

import java.io.IOException;
import java.nio.file.Path;

/** @author Nathan Ulmen
 * AssetImporter is a "user facing/outwardfacing" interface 
 * used to import .glb/gltf files into the Resource Systems?
 * 
 * */
final class AssetImporter {
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
