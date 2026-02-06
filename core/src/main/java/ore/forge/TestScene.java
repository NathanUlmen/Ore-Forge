package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import ore.forge.Input3D.CameraController;
import ore.forge.Input3D.FreeCamController;
import ore.forge.Render.*;

import java.util.ArrayList;

public class TestScene implements Screen {
    private Renderer renderer;
    private CameraController cameraController;
    private Camera camera;
    private RenderPart renderPart;
    private BasicRenderPass basicRenderPass;
    GLProfiler profiler;
    int i;

    public TestScene() {
        i = 0;
        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();
        basicRenderPass = new BasicRenderPass();

        AssetHandler handler = new AssetHandler();
        MeshHandle handle = handler.loadTestMesh();
        renderer = new Renderer(handler);

        renderer.renderPasses.add(basicRenderPass);
        camera = new  PerspectiveCamera();
        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 5f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.up.set(0f, 1f, 0f);
        camera.update(true);
        cameraController = new FreeCamController((PerspectiveCamera) camera);



        renderPart = RenderPart.defaultRenderPart(handle);
        renderPart.transform = renderPart.transform.translate(1,1,1).scale(5, 5, 5);
        MaterialHandle materialHandle = new MaterialHandle();
        materialHandle.shader = renderer.renderPasses.getFirst().currentShader;
        renderPart.material = materialHandle;

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        cameraController.update(delta);
        camera.update(true);
        ArrayList<RenderPart> renderParts = new ArrayList<>();
        renderParts.add(renderPart);


        Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);



        renderer.render(renderParts, camera);
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
