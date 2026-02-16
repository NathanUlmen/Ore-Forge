package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.profiling.Profiler;
import ore.forge.engine.render.*;
import ore.forge.game.input.CameraController;
import ore.forge.game.input.FreeCamController;
import ore.forge.engine.profiling.Stopwatch;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestScene implements Screen {
    private Renderer renderer;
    private CameraController cameraController;
    private Camera camera;
    private BasicRenderPass basicRenderPass;
    private GLProfiler profiler;
    private Stopwatch stopwatch;

    private float rotationDeg = 0f;
    private float rotationSpeedDegPerSec = 45f; // tweak
    private final Vector3 rotationAxis = new Vector3(1, 1, 1);

    // Keep all parts around (don’t recreate every frame)
    private final ArrayList<RenderPart> renderParts = new ArrayList<>(1_000);

    public TestScene() {
        stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        profiler = new GLProfiler(Gdx.graphics);
        profiler.enable();

        basicRenderPass = new BasicRenderPass();

        AssetHandler handler = new AssetHandler();
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
        final int cols = 50;             // 40 * 25 = 1000
        final int rows = 50;
        final float spacing = 3.0f;      // distance between instances
        final float scale = 1.0f;

        // Center the grid around (0,0)
        final float gridWidth = (cols - 1) * spacing;
        final float gridHeight = (rows - 1) * spacing;
        final float startX = -gridWidth * 0.5f;
        final float startY = -gridHeight * 0.5f;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                var meshHandles = handler.meshHandles;
                MeshHandle meshHandle = meshHandles.get(x % meshHandles.size());
                RenderPart part = RenderPart.defaultRenderPart(meshHandle);

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

        rotationDeg = (rotationDeg + rotationSpeedDegPerSec * delta) % 360f;

        // rotate renderParts
        for (RenderPart part : renderParts) {
            part.transform.rotate(rotationAxis, rotationSpeedDegPerSec * delta);
        }

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        renderer.render(renderParts, camera);
        System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());
        stopwatch.stop();
        System.out.println("Draw Calls: " + profiler.getDrawCalls());
        profiler.reset();
        Profiler.INSTANCE.log(stopwatch.elapsed(), Gdx.graphics.getFramesPerSecond());
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

