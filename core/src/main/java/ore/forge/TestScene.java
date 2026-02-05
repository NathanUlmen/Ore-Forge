package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import ore.forge.Input3D.CameraController;
import ore.forge.Input3D.FreeCamController;
import ore.forge.Render.*;

import java.util.ArrayList;

public class TestScene implements Screen {
    private Renderer renderer;
    private CameraController cameraController;
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
        cameraController = new FreeCamController(new PerspectiveCamera());
        var cam = cameraController.getCamera();
        cam.near = 0.1f;
        cam.position.set(0,0,10);
        cam.lookAt(0,0,0);
        cam.update();


        renderPart = RenderPart.defaultRenderPart(handle);
        renderPart.transform = renderPart.transform.translate(1,1,1).scale(5, 5, 5);
        MaterialHandle materialHandle = new MaterialHandle();
        materialHandle.shader = renderer.renderPasses.getFirst().currentShader;
        renderPart.material = materialHandle;

        cameraController.getCamera().lookAt(0,0,0);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        cameraController.update(delta);
        cameraController.getCamera().update();
        ArrayList<RenderPart> renderParts = new ArrayList<>();
        renderParts.add(renderPart);


        Gdx.gl.glClearColor(1f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);


        renderer.render(renderParts, cameraController.getCamera());
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
