package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import ore.forge.Input3D.CameraController;
import ore.forge.Input3D.FreeCamController;
import ore.forge.Render.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestScene implements Screen {
    private Renderer renderer;
    private CameraController cameraController;
    private Camera camera;
    private BasicRenderPass basicRenderPass;
    private GLProfiler profiler;
    private Stopwatch stopwatch;

    // Keep all parts around (don’t recreate every frame)
    private final ArrayList<RenderPart> renderParts = new ArrayList<>(1000);

    public TestScene() {
        stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        basicRenderPass = new BasicRenderPass();

        AssetHandler handler = new AssetHandler();
        MeshHandle handle = handler.meshHandles.getFirst();
        renderer = new Renderer();
        renderer.addRenderPass(basicRenderPass);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 60f); // pull back so you can see the grid
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.up.set(0f, 1f, 0f);
        camera.update(true);

        cameraController = new FreeCamController((PerspectiveCamera) camera);

        // Shared material (same shader)
        MaterialHandle materialHandle = new MaterialHandle();
        materialHandle.shader = renderer.renderPasses().getFirst().currentShader;

        // ---- Build 1000 parts in a grid ----
        final int cols = 10;             // 40 * 25 = 1000
        final int rows = 10;
        final float spacing = 3.0f;      // distance between instances
        final float scale = 1.0f;

        // Center the grid around (0,0)
        final float gridWidth = (cols - 1) * spacing;
        final float gridHeight = (rows - 1) * spacing;
        final float startX = -gridWidth * 0.5f;
        final float startY = -gridHeight * 0.5f;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                RenderPart part = RenderPart.defaultRenderPart(handle);

                // IMPORTANT: don’t mutate an existing transform with translate() chaining if it accumulates
                // Create a fresh transform per part (setToTranslation + scale)
                float px = startX + x * spacing;
                float py = startY + y * spacing;
                float pz = 0f;

                part.transform.idt();
                part.transform.translate(px, py, pz);
                part.transform.scale(scale, scale, scale);

                part.material = materialHandle;

                renderParts.add(part);
            }
        }
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        stopwatch.restart();
        cameraController.update(delta);
        camera.update(true);

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        renderer.render(renderParts, camera);
        System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());
        stopwatch.stop();
        Profiler.INSTANCE.log(stopwatch.getElapsedTime(), Gdx.graphics.getFramesPerSecond());
    }

    @Override public void resize(int width, int height) {
        if (camera instanceof PerspectiveCamera pc) {
            pc.viewportWidth = width;
            pc.viewportHeight = height;
            pc.update(true);
        }
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        profiler.disable();
    }
}

