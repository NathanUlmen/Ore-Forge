package ore.forge.Render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.utils.BufferUtils;

import java.util.List;

import static ore.forge.Render.AssetHandler.STRIDE_BYTES;

public class MeshBuilder {
    private int masterVbo;
    private int masterEbo;

    public MeshBuilder() {

    }

    public void buildMeshes(List<PackedMesh> meshes, List<MeshHandle> out) {

    }




    public int masterVBO() {
        return masterVbo;
    }

    public int masterEBO() {
        return masterEbo;
    }
}
