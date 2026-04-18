package ore.forge.engine.definitions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MeshDataSerializer {
    private final Kryo kryo;

    public MeshDataSerializer() {
        kryo = new Kryo();

        kryo.register(AssetGUID.class);
        kryo.register(AssetRecord.class);
        kryo.register(AssetType.class);
        kryo.register(int[].class);
        kryo.register(MeshData.class, new MeshDataKryoSerializer());
    }

    public void writeObject(MeshData meshData, Output output) {
        Objects.requireNonNull(meshData, "meshData");
        Objects.requireNonNull(output, "output");
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

    private static class MeshDataKryoSerializer extends Serializer<MeshData> {
        @Override
        public void write(Kryo kryo, Output output, MeshData meshData) {
            Objects.requireNonNull(meshData, "meshData");
            kryo.writeObject(output, meshData.record());

            FloatBuffer vbo = meshData.vbo().duplicate();
            vbo.rewind();
            int vboLen = vbo.remaining();
            output.writeInt(vboLen);
            for (int i = 0; i < vboLen; i++) {
                output.writeFloat(vbo.get());
            }

            IntBuffer ebo = meshData.ebo().duplicate();
            ebo.rewind();
            int eboLen = ebo.remaining();
            output.writeInt(eboLen);
            for (int i = 0; i < eboLen; i++) {
                output.writeInt(ebo.get());
            }
        }

        @Override
        public MeshData read(Kryo kryo, Input input, Class<? extends MeshData> type) {
            AssetRecord record = kryo.readObject(input, AssetRecord.class);

            int vboLen = input.readInt();
            FloatBuffer vbo = FloatBuffer.allocate(vboLen);
            for (int i = 0; i < vboLen; i++) {
                vbo.put(input.readFloat());
            }
            vbo.flip();

            int eboLen = input.readInt();
            IntBuffer ebo = IntBuffer.allocate(eboLen);
            for (int i = 0; i < eboLen; i++) {
                ebo.put(input.readInt());
            }
            ebo.flip();

            return new MeshData(record, vbo, ebo);
        }
    }
}
