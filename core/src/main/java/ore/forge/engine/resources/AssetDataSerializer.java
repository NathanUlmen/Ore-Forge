package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetDataSerializer {
    private static final String LOG_TAG = AssetDataSerializer.class.getName();
    private static final int POOL_MAX = 1;
    private final Pool<Kryo> kryoPool;
    private final ExecutorService threadPool;

    public AssetDataSerializer(int poolMax) {
        threadPool = Executors.newFixedThreadPool(poolMax);
        kryoPool = new Pool<>(true, true, poolMax) {
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
                        long hi = input.readLong();
                        long lo = input.readLong();
                        return new UUID(hi, lo);
                    }
                });

                kryo.register(AssetSourceKey.class);
                kryo.register(AssetType.class);
                kryo.register(int[].class);

                kryo.register(MeshData.class, new MeshDataSerializer.MeshDataKryoSerializer());
                //TODO: Register other loaders for each type of CpuAssetData.
                kryo.register(TextureData.class, new Serializer<TextureData>() {
                    @Override
                    public void write(Kryo kryo, Output output, TextureData object) {
                        output.writeInt(object.encodedBytes().length);
                        output.writeBytes(object.encodedBytes());
                    }

                    @Override
                    public TextureData read(Kryo kryo, Input input, Class type) {
                        int length = input.readInt();
                        byte[] bytes = input.readBytes(length);
                        return new TextureData(bytes);
                    }
                });

                return kryo;
            }
        };
    }

    public AssetDataSerializer() {
        this(POOL_MAX);
    }

    public void writeObject(CpuAssetData assetData, Output output) {
        Kryo kryo = kryoPool.obtain();
        kryo.writeObject(output, assetData);
        output.flush();
    }

    public CompletableFuture<CpuAssetData> load(AssetArtifact assetArtifact) {
        return CompletableFuture.supplyAsync(() -> {
            Kryo kryo = kryoPool.obtain();
            try (Input input = new Input(Files.newInputStream(assetArtifact.filepath()))) {
                return switch (assetArtifact.type()) {
                    case MESH -> kryo.readObject(input, MeshData.class);
                    case MATERIAL -> kryo.readObject(input, MaterialData.class);
                    case TEXTURE -> kryo.readObject(input, TextureData.class);
                    case ANIMATION -> kryo.readObject(input, AnimationData.class);
                };
            } catch (IOException e) {
                Gdx.app.error(LOG_TAG, "Failed to read data from: " + assetArtifact.filepath(), e);
                throw new RuntimeException("Failed to read data from: " + assetArtifact.filepath(), e);
            } finally {
                kryoPool.free(kryo);
            }
        }, threadPool);
    }

}
