package ore.forge.engine.definitions;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ore.forge.engine.MeshData;
import ore.forge.engine.importing.AssetSourceKey;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

public class MeshDataSerializer {
    private final Kryo kryo;

    public MeshDataSerializer() {
        kryo = new Kryo();

        kryo.register(UUID.class, new Serializer<UUID>() {
            @Override
            public void write(Kryo kryo, Output output, UUID object) {
                output.writeLong(object.getMostSignificantBits());
                output.writeLong(object.getLeastSignificantBits());
            }

            @Override
            public UUID read(Kryo kryo, Input input, Class<? extends UUID> type) {
                long hi =  input.readLong();
                long lo = input.readLong();
                return new UUID(hi, lo);
            }
        });

        kryo.register(AssetSourceKey.class);
        kryo.register(AssetType.class);
        kryo.register(int[].class);
        kryo.register(MeshData.class, new MeshDataKryoSerializer());
    }

    public void writeObject(MeshData meshData, Output output) {
        kryo.writeObject(output, meshData);
        output.flush();
    }

    public MeshData readObject(String inputFile) {
        return readObject(Path.of(inputFile));
    }

    public MeshData readObject(Path inputFile) {
        Objects.requireNonNull(inputFile, "inputFile");
        try (Input input = new Input(Files.newInputStream(inputFile))) {
            return kryo.readObject(input, MeshData.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read mesh data from " + inputFile, e);
        }
    }

    public static class MeshDataKryoSerializer extends Serializer<MeshData> {
        @Override
        public void write(Kryo kryo, Output output, MeshData meshData) {
            //VBO
            output.writeInt(meshData.vbo().length);
            output.writeFloats(meshData.vbo(), 0, meshData.vbo().length);

            //IBO
            output.writeInt(meshData.ibo().length);
            output.writeShorts(meshData.ibo(), 0, meshData.ibo().length);

            //VertexAttributes
            output.writeInt(meshData.attributes().size());
            for (VertexAttribute attribute : meshData.attributes()) {
                output.writeInt(attribute.usage);
                output.writeInt(attribute.numComponents);
                output.writeString(attribute.alias);
            }
        }

        @Override
        public MeshData read(Kryo kryo, Input input, Class<? extends MeshData> type) {
            //VBO
            int length = input.readInt();
            float[] vbo = input.readFloats(length);

            //IBO
            int iboLen = input.readInt();
            short[] ebo = input.readShorts(iboLen);


            //Vertex Attributes
            int numAttributes = input.readInt();
            VertexAttribute[] attributes = new VertexAttribute[numAttributes];
            for (int i = 0; i < numAttributes; i++) {
                int usage = input.readInt();
                int numComponents = input.readInt();
                String alias = input.readString();
                attributes[i] = new VertexAttribute(usage, numComponents, alias);
            }

            return new MeshData(new VertexAttributes(attributes), vbo, ebo);
        }
    }
}
