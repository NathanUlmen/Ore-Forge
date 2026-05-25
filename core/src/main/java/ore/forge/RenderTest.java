package ore.forge;

import com.badlogic.gdx.Screen;
import ore.forge.engine.MeshData;
import ore.forge.engine.definitions.AssetType;
import ore.forge.engine.definitions.MeshDataSerializer;
import ore.forge.engine.importing.AssetArtifact;
import ore.forge.engine.importing.AssetRegistry;
import ore.forge.engine.importing.AssetSourceKey;

public class RenderTest implements Screen {
    AssetRegistry registry;



    public RenderTest() {
        registry = new AssetRegistry();
        AssetSourceKey key = new AssetSourceKey();
        key.setAssetType(AssetType.MESH);
        key.setLogicalName("");
        AssetArtifact artifact = registry.lookUp(key);
        MeshDataSerializer serializer = new MeshDataSerializer();
        MeshData data = serializer.readObject(artifact.filepath());
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
