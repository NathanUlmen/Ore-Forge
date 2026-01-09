package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.*;
import ore.forge.Input3D.FreeCamController;
import ore.forge.Input3D.InputHandler;
import ore.forge.Input3D.IsometricCameraController;
import ore.forge.Items.ItemDefinition;
import ore.forge.Shaders.CustomShaderProvider;
import ore.forge.UI.UI;

import java.util.ArrayList;
import java.util.List;

public class Gameplay3D implements Screen {
    // Rendering
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final FreeCamController freeCamContronller;
    public static final ArrayList<ModelInstance> modelInstances = new ArrayList<>();
    public static final ArrayList<EntityInstance> entityInstances = new ArrayList<>();
    private final PhysicsWorld physicsWorld = PhysicsWorld.instance();
    private final CollisionManager collisionManager;
    private btRigidBody cubeBody;
    private ItemDefinition spawner;
    private InputHandler inputHandler;

    private UI ui;

    private final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0); // y=0 plane
    private final Vector3 intersection = new Vector3();
    private final Vector3 rayFrom = new Vector3();
    private final Vector3 rayTo = new Vector3();

    private float rotationAngle = 0;

    public Gameplay3D() {
        // Config ModelBatch
        modelBatch = new ModelBatch(new CustomShaderProvider());

        // Config Model Instance list

        // Config Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .8f, .8f, .8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // Initialize collision Manager
        collisionManager = new CollisionManager(null);

        // Create Plane:
        Matrix4 planeTransform = new Matrix4();
        planeTransform.setTranslation(0, -0.5f, 0);
        Model model = createBox(100, 1f, 100);
        ModelInstance groundModelInstance = new ModelInstance(model);
        btCollisionShape groundShape = new btBoxShape(new Vector3(50f, 0.5f, 50f));
        btRigidBody groundRigidBody = createStaticBody(groundModelInstance, groundShape);
        VisualComponent visualComponent = new VisualComponent(groundModelInstance);
        // visualComponent.attributes = new GridAttribute(GridAttribute.ID);
        var rigidBodies = new ArrayList<btCollisionObject>();
        rigidBodies.add(groundRigidBody);
        EntityInstance planeInstance = new EntityInstance(rigidBodies, visualComponent);
        planeInstance.setTransform(planeTransform);
        physicsWorld.dynamicsWorld().addRigidBody(groundRigidBody,
                CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY),
                CollisionRules.combineBits(CollisionRules.ORE));
        entityInstances.add(planeInstance);

        // Create Static test items from JSON
        JsonReader jsonReader = new JsonReader();
        JsonValue value = jsonReader.parse(Gdx.files.internal("Items/3DTestItem.json"));
        this.spawner = ItemDefinition.createDefinition(value);

        Matrix4 transform = new Matrix4();
        value = jsonReader.parse(Gdx.files.internal("Items/3DTestDropper.json"));
        ItemDefinition dropperSpawner = ItemDefinition.createDefinition(value);
        EntityInstance instance1 = EntityInstanceCreator.createInstance(dropperSpawner);
        transform.setTranslation(0, 8, 0);
        instance1.place(transform.cpy());
        for (btCollisionObject object : instance1.entityPhysicsBodies) {
            if (object.userData == null) {
                object.userData = "No Longer Null";
            }
            physicsWorld.dynamicsWorld().addCollisionObject(object);
        }
        entityInstances.add(instance1);

        // Config UI
        List<ItemDefinition> allItems = new ArrayList<>();
        allItems.add(dropperSpawner);
        allItems.add(spawner);
        ui = new UI(allItems);
        Gdx.input.setInputProcessor(ui);

        // Config camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; // Min distance can see/draw
        camera.far = 1000f; // Max distance can see/draw
        camera.position.set(0, 10, 10);
        camera.lookAt(0, 0, 0);

        // Config cameraController;
        freeCamContronller = new FreeCamController(camera);

        // Configure Input Handler
        inputHandler = new InputHandler(new IsometricCameraController(camera), ui);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // Process Input
        inputHandler.update(delta);
        camera.update();

        // Process input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {// rotate item
            rotationAngle += 90;
        }
        // Place item
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 position = getMouseGroundPosition(camera);
            // Spawn an instance of the item and add it to the physics simulation
            EntityInstance instance = EntityInstanceCreator.createInstance(spawner);
            Matrix4 transform = new Matrix4().setToTranslation(position);
            transform.rotate(Vector3.Y, rotationAngle % 360); // Bound it to 360
            instance.place(transform);
            // modelInstances.add(instance.visualComponent.modelInstance);
            for (btCollisionObject object : instance.entityPhysicsBodies) {
                physicsWorld.dynamicsWorld().addCollisionObject(object,
                        object instanceof btRigidBody ? CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY)
                                : CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR));
            }
            entityInstances.add(instance);
        }

        // Physics simulation Step
        physicsWorld.dynamicsWorld().stepSimulation(delta, 0);

        for (var instance : entityInstances) {
            var modelInstance = instance.visualComponent.modelInstance;
            for (int i = 0; i < instance.entityPhysicsBodies.size(); i++) {
                if (instance.entityPhysicsBodies.get(i) instanceof btRigidBody rb) {
                    rb.getMotionState().getWorldTransform(modelInstance.transform);
                }
            }
        }

        // Render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);

        modelBatch.begin(camera);
        for (var instance : entityInstances) {
            var modelInstance = instance.visualComponent.modelInstance;
            modelBatch.render(modelInstance, environment);
        }
        modelBatch.end();

        ui.act();
        ui.getViewport().apply();
        ui.draw();

        collisionManager.updateTouchingEntities(delta);
        TimerUpdater.update(delta);

        // physicsWorld.drawDebug(camera);
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

}
