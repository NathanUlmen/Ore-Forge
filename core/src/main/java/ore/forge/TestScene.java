package ore.forge;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import ore.forge.engine.*;
import ore.forge.engine.importing.AssetID;
import ore.forge.engine.importing.AssetRegistry;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.components.WorldTransformC;
import ore.forge.engine.components.definitions.RenderCDefinition;
import ore.forge.engine.components.definitions.WorldTransformDefinition;
import ore.forge.engine.definitions.BoxShapeIR;
import ore.forge.engine.definitions.PhysicsDefinition;
import ore.forge.engine.definitions.PlaneShapeIR;
import ore.forge.engine.render.GpuResource;
import ore.forge.engine.render.MaterialHandle;
import ore.forge.engine.render.RenderPart;
import ore.forge.engine.render.*;
import ore.forge.engine.render.passes.BasicRenderPass;
import ore.forge.game.input.CameraController;
import ore.forge.game.input.FreeCamController;
import ore.forge.engine.profiling.Stopwatch;
import ore.forge.engine.systems.PostPhysicsTransformSyncSystem;
import ore.forge.engine.systems.PrePhysicsTransformSyncSystem;
import ore.forge.engine.systems.RenderPrepSystem;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TestScene implements Screen {
    private static final String LOG_TAG = TestScene.class.getSimpleName();
    private static final float FRAME_LOG_INTERVAL_SEC = 1.0f;

    private Renderer renderer;
    private CameraController cameraController;
    private Camera camera;
    private BasicRenderPass basicRenderPass;
    private Stopwatch stopwatch;
    private Stage harnessStage;
    private VisWindow harnessWindow;
    private VisTable builderPreviewContainer;

    private Engine engine;
    private PhysicsWorld physicsWorld;
    private ImmutableArray<Entity> renderEntities;

    private float frameLogAccumulatorSec = 0f;
    private long frameTimeTotalMs = 0L;
    private long maxFrameTimeMs = 0L;
    private int frameSamples = 0;
    private static final String TEST_SCHEMA_PATH = "TestSchema.json";

    private static final int GRID_COLS = 25;
    private static final int GRID_ROWS = 25;
    private static final int GRID_LAYERS = 4;
    private static final float CUBE_SPACING = 2.2f;
    private static final float LAYER_HEIGHT = 2.1f;
    private static final float DROP_HEIGHT = 3f;
    private static final float GROUND_RENDER_Y = -0.5f;

    private final ArrayList<RenderPart> renderParts = new ArrayList<>(GRID_COLS * GRID_ROWS * GRID_LAYERS + 1);

    public TestScene(GpuResourceManager resourceManager, AssetRegistry assetRegistry) {
        stopwatch = new Stopwatch(TimeUnit.MILLISECONDS);
        engine = new Engine();
        physicsWorld = PhysicsWorld.instance();

        basicRenderPass = new BasicRenderPass();

        renderer = new Renderer(resourceManager);
        renderer.addRenderPass(basicRenderPass);

        camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 60f); // pull back so you can see the grid
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 2000f;
        camera.up.set(0f, 1f, 0f);
        camera.update(true);

        cameraController = new FreeCamController((PerspectiveCamera) camera);
        initializeHarness();
        initializeEngine();
        populateScene(resourceManager, assetRegistry);
        renderEntities = engine.getEntitiesFor(Family.all(RenderC.class, WorldTransformC.class).get());
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
        engine.getSystem(PrePhysicsTransformSyncSystem.class).update(delta);
        physicsWorld.dynamicsWorld().stepSimulation(delta, 3, 1f / 60f);
        engine.getSystem(PostPhysicsTransformSyncSystem.class).update(delta);
        engine.getSystem(RenderPrepSystem.class).update(delta);
        rebuildRenderParts();

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
        for (Entity entity : engine.getEntitiesFor(Family.all(PhysicsC.class).get())) {
            PhysicsC physics = entity.getComponent(PhysicsC.class);
            if (physics.collisionObject == null) {
                continue;
            }

            switch (physics.bodyType) {
                case RIGID -> physicsWorld.dynamicsWorld().removeRigidBody(physics.asRigidBody());
                case GHOST -> physicsWorld.dynamicsWorld().removeCollisionObject(physics.collisionObject);
            }
        }
        harnessStage.dispose();
    }

    private void initializeEngine() {
        engine.addSystem(new PrePhysicsTransformSyncSystem());
        engine.addSystem(new PostPhysicsTransformSyncSystem());
        engine.addSystem(new RenderPrepSystem());
    }

    private void populateScene(GpuResourceManager resourceManager, AssetRegistry assetRegistry) {
        Handle<GpuResource> meshHandle = null;
        Handle<GpuResource> textureHandle = null;

        for (AssetID id : assetRegistry.getIDs()) {
            AssetData data = resourceManager.retrieveData(id);
            switch (data) {
                case MeshData ignored -> meshHandle = resourceManager.getHandle(id);
                case TextureData ignored -> textureHandle = resourceManager.getHandle(id);
                default -> {
                }
            }
        }

        if (meshHandle == null || textureHandle == null) {
            throw new IllegalStateException("TestScene requires one mesh and one texture in the asset registry.");
        }

        MaterialHandle material = new MaterialHandle();
        material.baseColorTexture = textureHandle;

        createGround(meshHandle, material);
        createCubeField(meshHandle, material);
    }

    private void createGround(Handle<GpuResource> meshHandle, MaterialHandle material) {
        Entity ground = createEntity(
            new WorldTransformDefinition(new Matrix4().setToTranslation(0f, GROUND_RENDER_Y, 0f)),
            new RenderCDefinition(meshHandle, material, new Vector3(80f, 1f, 80f), new Matrix4().idt()),
            new PhysicsDefinition(
                "ground",
                PhysicsBodyType.RIGID,
                PhysicsMotionType.STATIC,
                0f,
                1f,
                0.15f,
                new PlaneShapeIR(new Vector3(0f, 1f, 0f), 0f)
            )
        );
        engine.addEntity(ground);
    }

    private void createCubeField(Handle<GpuResource> meshHandle, MaterialHandle material) {
        final float gridWidth = (GRID_COLS - 1) * CUBE_SPACING;
        final float gridDepth = (GRID_ROWS - 1) * CUBE_SPACING;
        final float startX = -gridWidth * 0.5f;
        final float startZ = -gridDepth * 0.5f;
        final BoundingBox unitCubeBounds = new BoundingBox(
            new Vector3(-0.5f, -0.5f, -0.5f),
            new Vector3(0.5f, 0.5f, 0.5f)
        );

        int cubeIndex = 0;
        for (int layer = 0; layer < GRID_LAYERS; layer++) {
            float y = DROP_HEIGHT + layer * LAYER_HEIGHT;
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLS; col++) {
                    float x = startX + col * CUBE_SPACING;
                    float z = startZ + row * CUBE_SPACING;
                    Entity cube = createEntity(
                        new WorldTransformDefinition(new Matrix4().setToTranslation(x, y, z)),
                        new RenderCDefinition(meshHandle, material, new Vector3(1f, 1f, 1f), new Matrix4().idt()),
                        new PhysicsDefinition(
                            "cube-" + cubeIndex++,
                            PhysicsBodyType.RIGID,
                            PhysicsMotionType.DYNAMIC,
                            1f,
                            0.8f,
                            0.05f,
                            new BoxShapeIR(new BoundingBox(unitCubeBounds))
                        )
                    );
                    engine.addEntity(cube);
                }
            }
        }
    }

    private Entity createEntity(ComponentDefinition<?>... definitions) {
        Entity entity = new Entity();
        for (ComponentDefinition<?> definition : definitions) {
            entity.add(definition.create());
        }

        PhysicsC physics = entity.getComponent(PhysicsC.class);
        WorldTransformC worldTransform = entity.getComponent(WorldTransformC.class);
        if (physics != null && worldTransform != null) {
            physics.collisionObject.setWorldTransform(worldTransform.currentTransform);
            switch (physics.bodyType) {
                case RIGID -> physicsWorld.dynamicsWorld().addRigidBody(physics.asRigidBody());
                case GHOST -> physicsWorld.dynamicsWorld().addCollisionObject(physics.collisionObject);
            }
        }

        return entity;
    }

    private void rebuildRenderParts() {
        renderParts.clear();
        for (Entity entity : renderEntities) {
            renderParts.add(entity.getComponent(RenderC.class).renderPart);
        }
    }

}
