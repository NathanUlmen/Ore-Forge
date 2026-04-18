package ore.forge.engine;

import com.esotericsoftware.kryo.io.Output;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import ore.forge.engine.definitions.AssetRecord;
import ore.forge.engine.definitions.MeshData;
import ore.forge.engine.definitions.MeshDataSerializer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Nathan Ulmen
 * <p>
 * AssetLoader takes in .glb/gltf and produces a FlatBuffer for each asset in the container.
 * For each asset packed into a FlatBuffer it also generates an {@link AssetRecord} which it
 * appends to a registry that the engine will load on startup.
 * This is process is done during "Build" time.
 *
 *
 */
public class AssetLoader {
    private static final Set<String> MODEL_EXTENSIONS = Set.of("gltf", "glb");
    private static final Map<String, Integer> ATTRIBUTE_INDEX = Map.of(
        "POSITION", 0,
        "NORMAL", 1,
        "TANGENT", 2,
        "COLOR_0", 3,
        "TEXCOORD_0", 4,
        "JOINTS_0", 5,
        "WEIGHTS_0", 6
    );

    private static final Map<String, Integer> ATTRIBUTE_SIZE = Map.of(
        "POSITION", 3,
        "NORMAL", 3,
        "TANGENT", 4,
        "COLOR_0", 4,
        "TEXCOORD_0", 2,
        "JOINTS_0", 4,
        "WEIGHTS_0", 4
    );
    private static final int IMPORT_VERSION = 1;

    protected final List<AssetRecord> registry;
    private final List<Future<?>> submittedTasks;
    protected final ExecutorService executor;
    private String outputDir;

    public AssetLoader(int numThreads, String outputDir) {
        registry = Collections.synchronizedList(new ArrayList<>());
        submittedTasks = Collections.synchronizedList(new ArrayList<>());
        executor = Executors.newFixedThreadPool(numThreads);
        this.outputDir = outputDir;
    }

    /**
     * Loads all .glb/gltf files in a given directory.
     *
     *
     */
    public void loadDirectory(String directoryPath) {
        loadDirectory(Path.of(directoryPath));
    }

    public void loadDirectory(Path directoryPath) {
        try (Stream<Path> files = Files.list(directoryPath)) {
            files
                .filter(Files::isRegularFile)
                .filter(this::isModelFile)
                .forEach(file -> {
                    Future<?> task = executor.submit(() -> {
                        GltfModel gltfModel = loadModel(file);
                        try {
                            extractAssetData(gltfModel, file);
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    submittedTasks.add(task);
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list asset directory: " + directoryPath, e);
        }
    }

    private boolean isModelFile(Path file) {
        String extension = fileExtension(file);
        return MODEL_EXTENSIONS.contains(extension);
    }

    private String fileExtension(Path file) {
        String fileName = file.getFileName().toString();
        int extensionStart = fileName.lastIndexOf('.');
        if (extensionStart < 0 || extensionStart == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(extensionStart + 1).toLowerCase(Locale.ROOT);
    }

    private GltfModel loadModel(Path file) {
        try {
            return new GltfModelReader().read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void extractAssetData(GltfModel gltfModel, Path sceneFilePath) throws FileNotFoundException {
        extractMeshes(gltfModel, sceneFilePath);
        extractTextures(gltfModel, sceneFilePath);
        extractAnimations(gltfModel, sceneFilePath);
    }

    /**
     * Extract mesh data and put it into a format that engine can handle.
     * MeshModel
     *
     */
    public void extractMeshes(GltfModel gltfModel, Path sceneFilePath) {
        //Register meshes
        for (MeshModel meshModel : gltfModel.getMeshModels()) {
            AssetRecord assetRecord = new AssetRecord();
            assetRecord.setDisplayName(meshModel.getName());
            registry.add(assetRecord);

            for (MeshPrimitiveModel primitive : meshModel.getMeshPrimitiveModels()) {
                AttributeHolder[] buffers = new AttributeHolder[6];

                for (Map.Entry<String, AccessorModel> entry : primitive.getAttributes().entrySet()) {
                    String attributeName = entry.getKey();

                    Integer index = ATTRIBUTE_INDEX.get(attributeName);
                    Integer size = ATTRIBUTE_SIZE.get(attributeName);

                    if (index == null || size == null) {
                        throw new RuntimeException(
                            "Unknown attribute: " + attributeName +
                                " in " + meshModel.getName() +
                                " found in " + sceneFilePath
                        );
                    }

                    FloatBuffer data = getFloatBufferData(primitive, attributeName);
                    buffers[index] = new AttributeHolder(attributeName, data, size);
                }

                FloatBuffer finalizedVbo = interleaveVBO(buffers);

                MeshData data = new MeshData(assetRecord, finalizedVbo, IntBuffer.allocate(32));
                MeshDataSerializer serializer = new MeshDataSerializer();

                Path outFile = Path.of(outputDir, assetRecord.displayName() + ".bin");
                try {
                    Files.createDirectories(outFile.getParent());
                    try (Output output = new Output(Files.newOutputStream(outFile))) {
                        serializer.writeObject(data, output);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to write mesh data to " + outFile, e);
                }
            }
        }

    }

    private static FloatBuffer interleaveVBO(AttributeHolder[] holders) {
        if (holders[0] == null) {
            throw new IllegalStateException("Position Data not present.");
        }
        int totalCapacity = 0;
        for (AttributeHolder holder : holders) {
            if (holder != null) {
                totalCapacity += holder.floatBuffer.capacity();
            }
        }

        FloatBuffer finalizedVbo = FloatBuffer.allocate(totalCapacity);

        int vertexCount = holders[0].floatBuffer.capacity() / holders[0].strideLength;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            for (AttributeHolder holder : holders) {
                if (holder == null) { continue; }

                int base = vertex * holder.strideLength;
                for (int component = 0; component < holder.strideLength; component++) {
                    finalizedVbo.put(holder.floatBuffer.get(base + component));
                }
            }
        }

        finalizedVbo.flip();
        return finalizedVbo;
    }

    private static FloatBuffer getFloatBufferData(MeshPrimitiveModel primitive, String attribute) {
        AccessorModel model = primitive.getAttributes().get(attribute);
        AccessorData data = model.getAccessorData();

        if (data instanceof AccessorFloatData floatData) {
            int capacity = floatData.getTotalNumComponents();
            FloatBuffer bufferData = FloatBuffer.allocate(capacity);
            for (int i = 0; i < capacity; i++) {
                bufferData.put(floatData.get(i));
            }
            bufferData.flip();
            return bufferData;
        } else {
            throw new RuntimeException();
        }

    }

    private void extractMeshRec(GltfModel gltfModel, Path container) {

    }

    public void extractTextures(GltfModel gltfModel, Path container) {

    }

    public void extractAnimations(GltfModel gltfModel, Path container) {

    }

    /**
     * Writes the asset registry to disk.
     *
     *
     */
    public void end() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Executor failed to shutdown");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        for (Future<?> task : submittedTasks) {
            try {
                task.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(cause);
            }
        }
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    private static class AttributeHolder {
        private final String attributeType;
        private final int strideLength;
        private final FloatBuffer floatBuffer;

        public AttributeHolder(String attributeType, FloatBuffer buffer, int strideLength) {
            this.attributeType = attributeType;
            this.floatBuffer = buffer;
            this.strideLength = strideLength;
        }


    }

}
