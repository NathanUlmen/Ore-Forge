package ore.forge.engine;

import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.io.Output;
import de.javagl.jgltf.model.*;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.importing.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AssetExtractor {

    public static void extractAssets(GltfModel gltfModel, Path sourceFile, AssetRegistry assetRegistry) {
        AssetExtractor.extractMeshes(gltfModel, sourceFile, assetRegistry);
        AssetExtractor.extractTextures(gltfModel);
    }

    public static void extractMeshes(GltfModel gltfModel, Path sourceFile, AssetRegistry assetRegistry) {
        ArrayList<AssetCandidate> assets = new ArrayList<>();
        Path meshOutput = assetRegistry.getBakedDir().resolve("meshes");
        ensureDirectory(meshOutput);
        //Register meshes
        for (MeshModel meshModel : gltfModel.getMeshModels()) {
            AssetSourceKey assetSourceKey = new AssetSourceKey();
            assetSourceKey.setAssetName(meshModel.getName());
            assetSourceKey.setAssetType(AssetType.MESH);
            assetSourceKey.setLogicalName(sourceFile == null ? meshModel.getName() : containerName(sourceFile));
            if (sourceFile != null) {
                assetSourceKey.setSourcePath(sourceFile.toString());
            }
            assetSourceKey.setImportVersion(1);


            for (MeshPrimitiveModel primitive : meshModel.getMeshPrimitiveModels()) {
                AttributeHolder[] buffers = new AttributeHolder[VertexAttribute.values().length];

                for (Map.Entry<String, AccessorModel> entry : primitive.getAttributes().entrySet()) {
                    String attributeName = entry.getKey();
                    VertexAttribute attribute = VertexAttribute.valueOf(attributeName);
                    ByteBuffer data = entry.getValue().getAccessorData().createByteBuffer();
                    buffers[attribute.index()] = new AttributeHolder(attribute, data, attribute.sizeInBytes());
                }

                ByteBuffer finalizedVbo = interleaveVBO(buffers);
                IntBuffer finalizedEbo = createEBO(primitive);

                MeshData data = new MeshData(finalizedVbo, finalizedEbo);
                //TODO figure out dependencies...

                Path finalizedOutTarget = meshOutput.resolve(meshModel.getName() + ".meshbin");
                ensureDirectory(finalizedOutTarget.getParent());

                AssetArtifact artifact = new AssetArtifact(
                    finalizedOutTarget.toString(),
                    null,
                    assetSourceKey,
                    null
                );
                AssetCandidate candidate = new AssetCandidate(assetSourceKey, data, artifact);
                assets.add(candidate);
            }
        }

        MeshDataSerializer serializer = new MeshDataSerializer();
        for (AssetCandidate candidate : assets) {
            if (assetRegistry.createNewEntry(candidate)) {
                if (candidate.assetData() instanceof MeshData meshData) {
                    try (
                        OutputStream outputStream = Files.newOutputStream(candidate.artifact().filepath());
                        Output output = new Output(outputStream)) {
                        serializer.writeObject(meshData, output);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }

    private static String containerName(java.nio.file.Path sourceFile) {
        String fileName = sourceFile.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex).toLowerCase(Locale.ROOT);
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

    public static List<AssetCandidate> extractTextures(GltfModel gltfModel) {
        ArrayList<AssetCandidate> assets = new ArrayList<>();
        for (TextureModel textureModel : gltfModel.getTextureModels()) {
            //TODO
        }
        return assets;
    }

    public static Path ensureDirectory(Path dir) {
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


}
