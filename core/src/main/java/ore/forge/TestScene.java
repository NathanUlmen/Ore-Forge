package ore.forge;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextArea;
import com.kotcrab.vis.ui.widget.VisWindow;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import ore.forge.engine.*;
import ore.forge.engine.importing.AssetID;
import ore.forge.engine.importing.AssetRegistry;
import ore.forge.engine.render.*;
import ore.forge.engine.render.passes.BasicRenderPass;
import ore.forge.engine.serialization.ComponentLoader;
import ore.forge.game.input.CameraController;
import ore.forge.game.input.FreeCamController;
import ore.forge.engine.profiling.Stopwatch;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestScene implements Screen {
    private static final String LOG_TAG = TestScene.class.getSimpleName();
    private static final float FRAME_LOG_INTERVAL_SEC = 1.0f;

    private Renderer renderer;
    private CameraController cameraController;
    private Camera camera;
    private BasicRenderPass basicRenderPass;
    private GLProfiler profiler;
    private Stopwatch stopwatch;
    private Stage harnessStage;
    private VisWindow harnessWindow;
    private VisTable builderPreviewContainer;

    private Engine engine;

    private float rotationDeg = 0f;
    private float rotationSpeedDegPerSec = 45f; // tweak
    private float frameLogAccumulatorSec = 0f;
    private long frameTimeTotalMs = 0L;
    private long maxFrameTimeMs = 0L;
    private int frameSamples = 0;
    private final Vector3 rotationAxis = new Vector3(1, 1, 1);
    private static final String TEST_SCHEMA_PATH = "TestSchema.json";

    // Keep all parts around (don’t recreate every frame)
    private final ArrayList<RenderPart> renderParts = new ArrayList<>(1_000);

    public TestScene(GpuResourceManager resourceManager, AssetRegistry assetRegistry) {
        stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        engine = new Engine();

        basicRenderPass = new BasicRenderPass();

        renderer = new Renderer(resourceManager);
        renderer.addRenderPass(basicRenderPass);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 60f); // pull back so you can see the grid
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.up.set(0f, 1f, 0f);
        camera.update(true);

        cameraController = new FreeCamController((PerspectiveCamera) camera);
        initializeHarness();

        // Shared material (same shader)
        MaterialHandle materialHandle = new MaterialHandle();

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
                Handle<GpuResource> meshHandle = null;
                Handle<GpuResource> textureHandle = null;
                for (AssetID id : assetRegistry.getIDs()) {
                    switch (resourceManager.retrieveData(id)) {
                        case MeshData m -> meshHandle = resourceManager.getHandle(id);
                        case TextureData t -> textureHandle = resourceManager.getHandle(id);
                        default ->
                            throw new IllegalStateException("Unexpected value: " + resourceManager.retrieveData(id));
                    }
                }
                materialHandle.baseColorTexture =  textureHandle;
                RenderPart part = RenderPart.defaultRenderPart(meshHandle);
                part.material = materialHandle;

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
    public void show() {
    }

    private void initializeHarness() {
        if (!VisUI.isLoaded()) {
            VisUI.load(VisUI.SkinScale.X2);
        }

        harnessStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(new InputMultiplexer(harnessStage));

        harnessWindow = new VisWindow("UISchemaBuilder Harness");
        harnessWindow.setResizable(true);
        harnessWindow.setMovable(true);
        harnessWindow.setSize(640f, Math.min(760f, Gdx.graphics.getHeight() - 40f));
        harnessWindow.setPosition(20f, Gdx.graphics.getHeight() - harnessWindow.getHeight() - 20f);

        VisTable content = new VisTable(true);
        content.top().left();
        content.defaults().growX().pad(8f);

        VisTextButton rebuildButton = new VisTextButton("Rebuild Preview");
        rebuildButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                rebuildBuilderPreview();
            }
        });

        builderPreviewContainer = new VisTable(true);
        builderPreviewContainer.top().left();
        builderPreviewContainer.defaults().growX().pad(6f);


        content.add(rebuildButton).left().width(220f).row();
        content.add(new VisScrollPane(builderPreviewContainer)).grow().minHeight(260f).row();

        harnessWindow.add(content).grow();
        harnessStage.addActor(harnessWindow);

        rebuildBuilderPreview();
    }

    private void rebuildBuilderPreview() {
        builderPreviewContainer.clearChildren();
        UISchemaBuilder builder = new UISchemaBuilder();
        Actor preview = builder.build(TEST_SCHEMA_PATH);
        builderPreviewContainer.add(preview).growX().top().left().row();
    }

    private String loadSchemaSource() {
        return Gdx.files.internal(TEST_SCHEMA_PATH).readString();
    }

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
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//        harnessStage.act(delta);
//        harnessStage.draw();

        stopwatch.stop();
        trackFrameTime(delta, stopwatch.elapsed());
    }

    private void trackFrameTime(float delta, long frameTimeMs) {
        frameLogAccumulatorSec += delta;
        frameTimeTotalMs += frameTimeMs;
        maxFrameTimeMs = Math.max(maxFrameTimeMs, frameTimeMs);
        frameSamples++;

        if (frameLogAccumulatorSec < FRAME_LOG_INTERVAL_SEC) {
            return;
        }

        float averageFrameTimeMs = frameSamples == 0 ? 0f : (float) frameTimeTotalMs / frameSamples;
        Gdx.app.log(
            LOG_TAG,
            String.format(
                "frame avg=%.2fms max=%dms fps=%d samples=%d",
                averageFrameTimeMs,
                maxFrameTimeMs,
                Gdx.graphics.getFramesPerSecond(),
                frameSamples
            )
        );

        frameLogAccumulatorSec = 0f;
        frameTimeTotalMs = 0L;
        maxFrameTimeMs = 0L;
        frameSamples = 0;
    }

    @Override
    public void resize(int width, int height) {
        if (camera instanceof PerspectiveCamera pc) {
            pc.viewportWidth = width;
            pc.viewportHeight = height;
            pc.update(true);
        }
        harnessStage.getViewport().update(width, height, true);
        harnessWindow.setHeight(Math.min(760f, height - 40f));
        harnessWindow.setPosition(20f, height - harnessWindow.getHeight() - 20f);
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
        profiler.disable();
        harnessStage.dispose();
    }

    public void loadEntities() {

    }

}
