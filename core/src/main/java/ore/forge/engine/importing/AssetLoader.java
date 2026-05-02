package ore.forge.engine.importing;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.esotericsoftware.kryo.io.Output;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import ore.forge.engine.VertexAttribute;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.MeshData;
import ore.forge.engine.definitions.MeshDataSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
 * For each asset packed into a FlatBuffer it also generates an {@link AssetSourceKey} which it
 * appends to a registry that the engine will load on startup.
 * This is process is done during "Build" time.
 *
 *
 */
public class AssetLoader {
    private static final int DEFAULT_THREADS = 4;
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

    //Size in Bytes
    private static final Map<String, Integer> ATTRIBUTE_SIZE = Map.of(
        "POSITION", Float.BYTES * 3,  // vec3 float
        "NORMAL", Float.BYTES * 3,     // vec3 float
        "TANGENT", Float.BYTES * 4,    // vec4 float
        "COLOR_0", Float.BYTES * 4,   // often vec4 float
        "TEXCOORD_0", Float.BYTES * 2,  // vec2 float
        "WEIGHTS_0", Float.BYTES * 4  // vec4 float
    );
    private static final int IMPORT_VERSION = 1;

    protected final List<AssetSourceKey> registry;
    private final List<Future<?>> submittedTasks;
    protected final ExecutorService executor;

    /**
     * @param numThreads - Number of hardware threads the Asset Loader will use when loading assets.
     *
     */
    public AssetLoader(int numThreads) {
        registry = Collections.synchronizedList(new ArrayList<>());
        submittedTasks = Collections.synchronizedList(new ArrayList<>());
        executor = Executors.newFixedThreadPool(numThreads);
        //TODO read existing registry into memory
    }

    public AssetLoader(int numThreads, File registryFile) {
        this(numThreads);
        loadRegistry(registryFile);
    }

    public AssetLoader() {
        this(DEFAULT_THREADS);
    }

    /**
     * @param inputDir  - Directory to read all .gltf files from.
     * @param outputDir - Directory to put the "baked" assets into.
     *                  Loads all .gltf/.glb files from an intput file and outputs to an output directory.
     */
    public void loadDirectory(Path inputDir, Path outputDir) {
        try (Stream<Path> files = Files.list(inputDir)) {
            files
                .filter(Files::isRegularFile)
                .filter(this::isModelFile)
                .forEach(file -> {
                    Future<?> task = executor.submit(() -> {
                        GltfModel gltfModel = loadModel(file);
                        ExtractedAssetData assetData = extractAssetData(gltfModel, file);
                        flushExtracted(assetData, outputDir);
                    });
                    submittedTasks.add(task);
                });
        } catch (IOException e) {
            throw new RuntimeException("Failed to list asset directory: " + inputDir, e);
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

    public ExtractedAssetData extractAssetData(GltfModel gltfModel, Path sceneFilePath) {
        var meshes = extractMeshes(gltfModel, sceneFilePath);
        extractTextures(gltfModel, sceneFilePath);
        extractAnimations(gltfModel, sceneFilePath);
        var extractedAssetData = new ExtractedAssetData();
        extractedAssetData.meshes.addAll(meshes);
        return extractedAssetData;
        //return extracted data in one big ol struct
    }

    /**
     * Extract mesh data and put it into a format that engine can handle.
     */
    public List<MeshData> extractMeshes(GltfModel gltfModel, Path sceneFilePath) {
        List<MeshData> dataList = new ArrayList<>();
        //Register meshes
        for (MeshModel meshModel : gltfModel.getMeshModels()) {
            AssetSourceKey assetSourceKey = new AssetSourceKey();
            assetSourceKey.setAssetName(meshModel.getName());
            registry.add(assetSourceKey);
            assetSourceKey.setAssetType(AssetType.MESH);

            for (MeshPrimitiveModel primitive : meshModel.getMeshPrimitiveModels()) {
                AttributeHolder[] buffers = new AttributeHolder[6];

                for (Map.Entry<String, AccessorModel> entry : primitive.getAttributes().entrySet()) {
                    String attributeName = entry.getKey();
                    VertexAttribute attribute = VertexAttribute.valueOf(attributeName);
                    ByteBuffer data = entry.getValue().getAccessorData().createByteBuffer();
                    buffers[attribute.index()] = new AttributeHolder(attribute, data, attribute.sizeInBytes());
                }

                ByteBuffer finalizedVbo = interleaveVBO(buffers);
                IntBuffer finalizedEbo = createEBO(primitive);

                MeshData data = new MeshData(finalizedVbo, finalizedEbo);
                dataList.add(data);
            }
        }
        return dataList;
    }

    public static ByteBuffer interleaveVBO(AttributeHolder[] holders) {
        if (holders[0] == null) {
            throw new IllegalStateException("Position Data not present.");
        }
        int totalCapacity = 0;
        for (AttributeHolder holder : holders) {
            if (holder == null) {
                continue;
            }
            totalCapacity += holder.buffer().capacity();
        }

        ByteBuffer finalizedVbo = ByteBuffer.allocate(totalCapacity);

        int vertexCount = holders[0].buffer().capacity() / holders[0].strideLength();
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            for (AttributeHolder holder : holders) {
                if (holder == null) {
                    continue;
                }
                int base = vertex * holder.strideLength();
                for (int component = 0; component < holder.strideLength(); component++) {
                    finalizedVbo.put(holder.buffer().get(base + component));
                }
            }
        }

        finalizedVbo.flip();
        return finalizedVbo;
    }

    public static IntBuffer createEBO(MeshPrimitiveModel primitiveModel) {
        AccessorModel accessorModel = primitiveModel.getIndices();
        ByteBuffer indexBytes = accessorModel.getAccessorData().createByteBuffer();
        indexBytes.order(ByteOrder.LITTLE_ENDIAN); //gltf specifies little endian


        int count = accessorModel.getCount();
        int type = accessorModel.getComponentType();

        IntBuffer finalizedIndexBuffer = IntBuffer.allocate(count);
        switch (type) {
            case GltfConstants.GL_UNSIGNED_BYTE -> {
                for (int i = 0; i < count; i++) {
                    int val = indexBytes.get() & 0xFF;
                    finalizedIndexBuffer.put(val);
                }
            }
            case GltfConstants.GL_UNSIGNED_SHORT -> {
                for (int i = 0; i < count; i++) {
                    int val = indexBytes.getShort(i * Short.BYTES) & 0xFFFF;
                    finalizedIndexBuffer.put(val);
                }
            }
            case GltfConstants.GL_UNSIGNED_INT -> {
                for (int i = 0; i < count; i++) {
                    int val = indexBytes.getInt(i * Integer.BYTES);
                    finalizedIndexBuffer.put(val);
                }
            }
            default -> throw new RuntimeException("Unknown primitive type " + type);
        }
        finalizedIndexBuffer.flip();
        return finalizedIndexBuffer;
    }

    public void extractTextures(GltfModel gltfModel, Path container) {

    }

    public void extractAnimations(GltfModel gltfModel, Path container) {

    }

    private void flushExtracted(ExtractedAssetData extractedAssetData, Path outputDir) {
//        MeshDataSerializer serializer = new MeshDataSerializer();
//
//        for (MeshData meshData : extractedAssetData.meshes) {
//            Path outFile = outputDir.resolve(meshData.assetRecord().displayName() + ".bin");
//            try {
//                Files.createDirectories(outFile.getParent());
//                System.out.println(outFile);
//
//                try (Output output = new Output(Files.newOutputStream(outFile))) {
//                    serializer.writeObject(meshData, output);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to write mesh data to " + outFile, e);
//            }
//        }
    }

    /**
     * @param outputDir - Directory registry should be output to.
     *                  Flushes asset registry to a file in the specified directory
     */
    public void end(Path outputDir) {
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
        //TODO Flush our registry
    }

    @SuppressWarnings("unchecked")
    public void loadRegistry(File registryFile) {
        JsonReader reader = new JsonReader();
        JsonValue jsonValue = reader.parse(new FileHandle(registryFile));
        Json json = new Json();
        json.setSerializer(UUID.class, new Json.Serializer<>() {
            @Override
            public void write(Json json, UUID object, Class knownType) {
                json.writeValue(object.toString());
            }

            @Override
            public UUID read(Json json, JsonValue jsonData, Class type) {
                return UUID.fromString(jsonData.asString());
            }
        });
        ArrayList<?> registry = json.fromJson(ArrayList.class, AssetSourceKey.class, jsonValue.toString());
        this.registry.addAll((Collection<? extends AssetSourceKey>) registry);
    }

    public static class ExtractedAssetData {
        private List<AssetSourceKey> assetSourceKeys;
        private final List<MeshData> meshes;
        private final List<?> textures, animations;

        public ExtractedAssetData() {
            assetSourceKeys = new ArrayList<>();
            meshes = new ArrayList<>();
            textures = new ArrayList<>();
            animations = new ArrayList<>();
        }

    }

}
