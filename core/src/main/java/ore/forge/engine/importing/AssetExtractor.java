package ore.forge.engine.importing;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.esotericsoftware.kryo.io.Output;
import de.javagl.jgltf.model.*;
import ore.forge.engine.*;
import ore.forge.engine.definitions.AssetType;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * @author Nathan Ulmen
 * {@link AssetExtractor} provides a set of static methods that extract
 * {@link ore.forge.engine.AssetData} from a {@link GltfModel} and gives it to the {@link AssetRegistry}
 *
 */
public class AssetExtractor {

    enum BakedType {
        MESH_BIN,
        TEXTURE_BIN,
    }

    public static void extractAssets(GltfModel gltfModel, Path sourceFile, AssetRegistry assetRegistry) {
        AssetExtractor.extractMeshes(gltfModel, sourceFile, assetRegistry);
        AssetExtractor.extractTextures(gltfModel, sourceFile, assetRegistry);
    }

    public static void extractMeshes(GltfModel gltfModel, Path sourceFile, AssetRegistry assetRegistry) {
        ArrayList<AssetCandidate> assets = new ArrayList<>();
        Path meshOutput = assetRegistry.getBakedDir().resolve("meshes");
        ensureDirectory(meshOutput);
        //Register meshes
        for (MeshModel meshModel : gltfModel.getMeshModels()) {
            AssetSourceKey sourceKey = createAssetSourceKey(AssetType.MESH, meshModel, sourceFile);

            for (MeshPrimitiveModel primitive : meshModel.getMeshPrimitiveModels()) {
                AttributeHolder[] buffers = new AttributeHolder[VertexAttribute.values().length];
                for (Map.Entry<String, AccessorModel> entry : primitive.getAttributes().entrySet()) {
                    String attributeName = entry.getKey();
                    VertexAttribute attribute = VertexAttribute.valueOf(attributeName);
                    ByteBuffer data = entry.getValue().getAccessorData().createByteBuffer();
                    buffers[attribute.slot()] = new AttributeHolder(attribute, data, attribute.sizeInBytes());
                }

                float[] finalizedVbo = interleaveVBO(buffers);
                short[] finalizedEbo = createIBO(primitive);
                VertexAttributes attributes = createAttributes(buffers);

                MeshData data = new MeshData(attributes, finalizedVbo, finalizedEbo);
                //TODO figure out dependencies...

                assets.add(createCandidate(meshModel, meshOutput, sourceKey, data));
            }
        }

        bakeToDisk(assetRegistry, assets);

    }

    private static void bakeToDisk(AssetRegistry assetRegistry, ArrayList<AssetCandidate> assets) {
        AssetDataSerializer serializer = new AssetDataSerializer();
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

    private static AssetCandidate createCandidate(NamedModelElement modelElement, Path assetOutput, AssetSourceKey sourceKey, AssetData data) {
        Path finalizedOutTarget = assetOutput.resolve(modelElement.getName() + ".meshbin");
        ensureDirectory(finalizedOutTarget.getParent());

        AssetArtifact artifact = new AssetArtifact(
            finalizedOutTarget.toString(),
            null,
            sourceKey,
            null
        );
        return new AssetCandidate(sourceKey, data, artifact);
    }

    private static VertexAttributes createAttributes(AttributeHolder... attributes) {
        ArrayList<com.badlogic.gdx.graphics.VertexAttribute> gdxAttributes = new ArrayList<>();
        for (AttributeHolder attribute : attributes) {
            if (attribute != null) {
                gdxAttributes.add(attribute.type().toGdxAttribute());
            }
        }
        return new VertexAttributes(gdxAttributes.toArray(new com.badlogic.gdx.graphics.VertexAttribute[0]));
    }

    private static String containerName(java.nio.file.Path sourceFile) {
        String fileName = sourceFile.getFileName().toString();
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return fileName;
        }
        return fileName.substring(0, extensionIndex).toLowerCase(Locale.ROOT);
    }

    public static float[] interleaveVBO(AttributeHolder[] holders) {
        if (holders[VertexAttribute.POSITION.slot()] == null) {
            throw new IllegalStateException("Position Data not present.");
        }

        int vertexCount =
            holders[VertexAttribute.POSITION.slot()].buffer().capacity()
                / holders[VertexAttribute.POSITION.slot()].strideLength();

        int floatsPerVertex = 0;
        for (AttributeHolder holder : holders) {
            if (holder != null) {
                floatsPerVertex += holder.strideLength() / Float.BYTES;
            }
        }

        float[] finalizedVbo = new float[vertexCount * floatsPerVertex];

        int out = 0;
        for (int vertex = 0; vertex < vertexCount; vertex++) {
            for (AttributeHolder holder : holders) {
                if (holder == null) {
                    continue;
                }

                ByteBuffer buffer = holder.buffer().duplicate();
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                int base = vertex * holder.strideLength();
                int componentCount = holder.strideLength() / Float.BYTES;

                for (int component = 0; component < componentCount; component++) {
                    finalizedVbo[out++] = buffer.getFloat(base + component * Float.BYTES);
                }
            }
        }

        return finalizedVbo;
    }

    public static short[] createIBO(MeshPrimitiveModel primitiveModel) {
        AccessorModel accessorModel = primitiveModel.getIndices();
        ByteBuffer indexBytes = accessorModel.getAccessorData().createByteBuffer();
        indexBytes.order(ByteOrder.LITTLE_ENDIAN);

        int count = accessorModel.getCount();
        int type = accessorModel.getComponentType();

        short[] finalizedIndexBuffer = new short[count];

        switch (type) {
            case GltfConstants.GL_UNSIGNED_BYTE -> {
                for (int i = 0; i < count; i++) {
                    int value = indexBytes.get(i) & 0xFF;
                    finalizedIndexBuffer[i] = (short) value;
                }
            }
            case GltfConstants.GL_UNSIGNED_SHORT -> {
                for (int i = 0; i < count; i++) {
                    int value = indexBytes.getShort(i * Short.BYTES) & 0xFFFF;
                    finalizedIndexBuffer[i] = (short) value;
                }
            }
            case GltfConstants.GL_UNSIGNED_INT -> {
                for (int i = 0; i < count; i++) {
                    int value = indexBytes.getInt(i * Integer.BYTES);

                    if (value < 0 || value > 0xFFFF) {
                        throw new IllegalArgumentException(
                            "Index " + value + " cannot fit in GL_UNSIGNED_SHORT."
                        );
                    }

                    finalizedIndexBuffer[i] = (short) value;
                }
            }
            default -> throw new RuntimeException("Unknown primitive index type " + type);
        }

        return finalizedIndexBuffer;
    }

    public static void extractTextures(GltfModel gltfModel, Path sourceFile, AssetRegistry assetRegistry) {
        ArrayList<AssetCandidate> assets = new ArrayList<>();

        Path textureOutput = assetRegistry.getBakedDir().resolve("meshes");
        ensureDirectory(textureOutput);
        for (TextureModel textureModel : gltfModel.getTextureModels()) {
            AssetSourceKey assetSourceKey = createAssetSourceKey(AssetType.TEXTURE, textureModel, sourceFile);
            int byteLength = textureModel.getImageModel().getBufferViewModel().getByteLength();
            int offset = textureModel.getImageModel().getBufferViewModel().getByteOffset();

            ByteBuffer imageData = textureModel.getImageModel().getImageData();
            TextureData textureData = new TextureData(imageData, offset, byteLength);

            assets.add(createCandidate(textureModel, textureOutput, assetSourceKey, textureData));

        }

        bakeToDisk(assetRegistry, assets);
    }

    public static Path ensureDirectory(Path dir) {
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static AssetSourceKey createAssetSourceKey(AssetType assetType, NamedModelElement namedModelElement, Path sourceFile) {
        String assetName = namedModelElement.getName();
        String logicalName = sourceFile == null ? namedModelElement.getName() : containerName(sourceFile);
        return new AssetSourceKey(assetType, logicalName, assetName, sourceFile.toString(), AssetImporter.IMPORT_VERSION, null);
    }

}
