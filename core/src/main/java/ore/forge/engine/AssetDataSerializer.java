package ore.forge.engine;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.importing.AssetArtifact;
import ore.forge.engine.importing.AssetSourceKey;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class AssetDataSerializer {
    private static final int POOL_MAX = 3;
    private final Pool<Kryo> kryoPool;

    public AssetDataSerializer(int poolMax) {
        kryoPool = new  Pool<>(false, true, poolMax) {
            protected Kryo create() {
                Kryo kryo = new Kryo();

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

                kryo.register(MeshData.class, new MeshDataSerializer.MeshDataKryoSerializer());
                //TODO: Register other loaders for each type of AssetData.

                return kryo;
            }
        };
    }

    public AssetDataSerializer() {
        this(POOL_MAX);
    }

    public AssetData load(AssetArtifact assetArtifact) {
        Kryo kryo = kryoPool.obtain();
        try (Input input = new Input(Files.newInputStream(assetArtifact.filepath()))) {
            return switch (assetArtifact.sourceKey().assetType()) {
                case MESH -> kryo.readObject(input, MeshData.class);
                case MATERIAL -> kryo.readObject(input, MaterialData.class);
                case TEXTURE -> kryo.readObject(input, TextureData.class);
                case ANIMATION -> kryo.readObject(input, AnimationData.class);
            };
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException("Failed to read mesh data from: " + assetArtifact.filepath(), e);
        } finally {
            kryoPool.free(kryo);
        }
    }

}
