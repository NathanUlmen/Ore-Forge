package ore.forge.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.Components.PhysicsComponent;
import ore.forge.engine.Components.VisualComponent;
import ore.forge.engine.EntityInstance;
import ore.forge.game.EntityInstanceCreator;
import ore.forge.engine.PhysicsBody;
import ore.forge.game.CollisionRules;
import ore.forge.game.GameContext;
import ore.forge.game.PhysicsBodyData;
import ore.forge.game.input.InputHandler;
import ore.forge.game.input.IsometricCameraController;
import ore.forge.game.items.ItemDefinition;
import ore.forge.Shaders.CustomShaderProvider;
import ore.forge.game.behaviors.BodyLogic;
import ore.forge.game.ui.UI;

import java.util.ArrayList;
import java.util.List;

import static ore.forge.game.CollisionRules.ORE;

public class Gameplay3D implements Screen {
    // Rendering
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private GameContext context;
    private ItemDefinition spawner;
    private InputHandler inputHandler;
    private GLProfiler profiler;
    private static final float FIXED_STEP = 1/60f;
    private float accumulator;

    private int tickCount;
    private float totalTime;

    private UI ui;

    private final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0); // y=0 plane
    private final Vector3 intersection = new Vector3();

    public Gameplay3D(UI ui) {
        accumulator = 0;
        context = GameContext.INSTANCE;
        // Config ModelBatch
        modelBatch = new ModelBatch(new CustomShaderProvider());

        // Config Model Instance list

        // Config Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .8f, .8f, .8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // Create Plane:
        Matrix4 planeTransform = new Matrix4();
        planeTransform.setTranslation(0, -2f, 0);
        Model model = createBox(100, 3f, 100);
        ModelInstance groundModelInstance = new ModelInstance(model);
        btCollisionShape groundShape = new btBoxShape(new Vector3(50f, 1.5f, 50f));
        btRigidBody groundRigidBody = createStaticBody(groundModelInstance, groundShape);
        groundRigidBody.setFriction(100);
        VisualComponent visualComponent = new VisualComponent(groundModelInstance);
        // visualComponent.attributes = new GridAttribute(GridAttribute.ID);

        var rigidBodies = new ArrayList<PhysicsBody>();
        rigidBodies.add(new PhysicsBody(groundRigidBody, new Matrix4().translate(0, -.75f, 0), CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY), CollisionRules.combineBits(ORE)));
        PhysicsComponent physicsComponent = new PhysicsComponent(rigidBodies);
        EntityInstance planeInstance = new EntityInstance(null, physicsComponent, visualComponent);
        BodyLogic bodyLogic = new BodyLogic() {
            @Override
            public void register(GameContext context) {

            }

            @Override
            public void unregister(GameContext context) {

            }

            @Override
            public void attach(ItemDefinition definition, btCollisionObject collisionObject) {

            }

            @Override
            public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

            }

            @Override
            public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching) {
                if (timeTouching > 1f) {
                    context.entityManager.stageRemove(subject.parentEntityInstance);
                }
            }

            @Override
            public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

            }

            @Override
            public BodyLogic clone() {
                return null;
            }
        };
        groundRigidBody.userData = new PhysicsBodyData(planeInstance, null, bodyLogic, null);
            planeInstance.setTransform(planeTransform);
//        physicsWorld.dynamicsWorld().addRigidBody(groundRigidBody,
//                CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY),
//                CollisionRules.combineBits(CollisionRules.ORE));
        context.entityManager.stageAdd(planeInstance);

        // Create Static test items from JSON
        JsonReader jsonReader = new JsonReader();
        JsonValue value = jsonReader.parse(Gdx.files.internal("Items/3DTestItem.json"));
        this.spawner = ItemDefinition.createDefinition(value);

        Matrix4 transform = new Matrix4();
        value = jsonReader.parse(Gdx.files.internal("Items/3DTestDropper.json"));
        ItemDefinition dropperSpawner = ItemDefinition.createDefinition(value);
        EntityInstance instance1 = EntityInstanceCreator.createInstance(dropperSpawner);
        transform.setTranslation(0, 8, 0);
        instance1.setTransform(transform.cpy());
        context.entityManager.stageAdd(instance1);

        // Config UI
        List<ItemDefinition> allItems = new ArrayList<>();
        allItems.add(dropperSpawner);
        allItems.add(spawner);
        this.ui = ui;
        Gdx.input.setInputProcessor(ui);

        // Config camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; // Min distance can see/draw
        camera.far = 1000f; // Max distance can see/draw
        camera.position.set(0, 10, 10);
        camera.lookAt(0, 0, 0);

        // Configure Input Handler
        inputHandler = new InputHandler(new IsometricCameraController(camera), ui, context);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // Process Input
        inputHandler.update(delta);
        camera.update();
        accumulator += delta;

        while (accumulator >= FIXED_STEP) {
//            update game state
            context.update(FIXED_STEP);
            accumulator -= FIXED_STEP;
        }

        //Render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);

        float alpha = accumulator / delta;

        modelBatch.begin(camera);
        //Render all our entities
        for (EntityInstance instance : context.entityManager) {
            instance.syncRender(alpha);
            modelBatch.render(instance.visualComponent.modelInstance);
        }
        //Render preview entities
        for (EntityInstance instance : context.previewManager) {
            instance.syncRender(alpha);
            modelBatch.render(instance.visualComponent.modelInstance);
        }
        modelBatch.end();
//        context.physicsWorld.drawDebug(camera);

        ui.act();
        ui.getViewport().apply();
        ui.draw();
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

    private Model createBox(float width, float height, float depth, Color color) {
        ModelBuilder builder = new ModelBuilder();
        return builder.createBox(width, height, depth,
                new Material(ColorAttribute.createDiffuse(color)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    private Model createBox(float width, float height, float depth) {
        return createBox(width, height, depth, Color.GREEN);
    }

    private btRigidBody createStaticBody(ModelInstance instance, btCollisionShape shape) {
        Matrix4 transform = instance.transform;
        btDefaultMotionState motionState = new btDefaultMotionState(transform);
        btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(0, motionState,
                shape, Vector3.Zero);
        btRigidBody body = new btRigidBody(info);
        info.dispose();
        return body;
    }

    private btRigidBody createDynamicBody(ModelInstance instance, btCollisionShape shape, float mass) {
        Vector3 inertia = new Vector3(0, 0, 0);
        shape.calculateLocalInertia(mass, inertia);
        Matrix4 transform = instance.transform;
        btDefaultMotionState motionState = new btDefaultMotionState(transform);
        btRigidBody.btRigidBodyConstructionInfo info = new btRigidBody.btRigidBodyConstructionInfo(mass, motionState,
                shape, inertia);
        btRigidBody body = new btRigidBody(info);
        info.dispose();
        return body;
    }

    private Vector3 getMouseGroundPosition(Camera camera) {
        // Get mouse position in screen coordinates
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // Unproject to get the world-space ray
        Ray ray = camera.getPickRay(mouseX, mouseY);

        // Intersect the ray with the ground plane
        if (Intersector.intersectRayPlane(ray, groundPlane, intersection)) {
            // Clamp Y >= 0 just in case
            if (intersection.y < 0f)
                intersection.y = 0f;
            return new Vector3(MathUtils.floor(intersection.x), MathUtils.floor(intersection.y),
                    MathUtils.floor(intersection.z));
        } else {
            // No intersection (looking up at sky)
            return new Vector3(ray.origin);
        }
    }

    private void logTickRate(float deltaT) {
        tickCount++;
        totalTime +=  deltaT;
        System.out.println("Tick count over total time: " + tickCount / totalTime);
    }

}
