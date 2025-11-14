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
import ore.forge.Input.CameraController3D;
import ore.forge.Items.Experimental.DropperSpawner;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.UpgraderSpawner;

import java.util.ArrayList;

public class Gameplay3D implements Screen {
    //Rendering
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final CameraController3D cameraController3D;
    private final ArrayList<ModelInstance> modelInstances;
    private final PhysicsWorld physicsWorld = PhysicsWorld.instance();
    private final CollisionManager collisionManager;
    private btRigidBody cubeBody;
    private ItemSpawner spawner;


    private final Plane groundPlane = new Plane(new Vector3(0, 1, 0), 0); // y=0 plane
    private final Vector3 intersection = new Vector3();
    private final Vector3 rayFrom = new Vector3();
    private final Vector3 rayTo = new Vector3();

    private float rotationAngle = 0;

    public Gameplay3D() {
        //Config camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; //Min distance can see/draw
        camera.far = 1000f; //Max distance can see/draw
        camera.position.set(0, 10, -10);
        camera.lookAt(0, 0, 0);

        //Config cameraController;
        cameraController3D = new CameraController3D(camera);

        //Config ModelBatch
        modelBatch = new ModelBatch();

        //Config Model Instance list
        modelInstances = new ArrayList<>();

        //Config Environment
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .8f, .8f, .8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        //create cube and apply transform
        Matrix4 transform = new Matrix4();
        Model model = createBox(1.5f, 1.5f, 1.5f, Color.PINK);
        var instance = new ModelInstance(model);
        transform.translate(0, .75f, 0);
        instance.transform.set(transform);
        modelInstances.add(instance);

        //create plane and apply transform
        instance = new ModelInstance(createBox(50, 0.01f, 50));
        var t2 = new Matrix4();
        t2.translate(0, 0, 0);
        instance.transform.set(t2);
        modelInstances.add(instance);

        // Create collision shapes
        btCollisionShape boxShape = new btBoxShape(new Vector3(1.5f / 2, 1.5f / 2, 1.5f / 2));
        btCollisionShape planeShape = new btBoxShape(new Vector3(25f, 0.01f, 25f));

        // Create rigid bodies
        cubeBody = createDynamicBody(modelInstances.get(0), boxShape, 10f);
        cubeBody.setSleepingThresholds(0, 0);
        btRigidBody groundBody = createStaticBody(modelInstances.get(1), planeShape);
        groundBody.setFriction(1.7f);

        var ore = new Ore();
        cubeBody.userData = ore;
        ore.rigidBody = cubeBody;

        groundBody.userData = "IM DA GROUND";
        // Add to world
        physicsWorld.dynamicsWorld().addRigidBody(cubeBody, CollisionRules.combineBits(CollisionRules.ORE),
            CollisionRules.combineBits(CollisionRules.ORE, CollisionRules.ORE_PROCESSOR, CollisionRules.WORLD_GEOMETRY));
        physicsWorld.dynamicsWorld().addRigidBody(groundBody, CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY),
            CollisionRules.combineBits(CollisionRules.ORE));


        //Transform cube to be in the air.
        Matrix4 t3 = new Matrix4();
        cubeBody.getWorldTransform(t3);
        t3.setToTranslation(-10, 2f, 0);
        t3.rotate(1, 1, 1, 0f);
        cubeBody.setWorldTransform(t3);
        cubeBody.getMotionState().setWorldTransform(t3);

        //Initialize collision Manager
        collisionManager = new CollisionManager();

        JsonReader jsonReader = new JsonReader();
        JsonValue value = jsonReader.parse(Gdx.files.internal("Items/3DTestItem.json"));
        this.spawner = new UpgraderSpawner(value);

        value = jsonReader.parse(Gdx.files.internal("Items/3DTestDropper.json"));
        var dropperSpawner = new DropperSpawner(value);
        EntityInstance instance1 = dropperSpawner.spawnInstance();
        instance1.place(transform.cpy());
        for (btCollisionObject object : instance1.entityPhysicsBodies) {
            physicsWorld.dynamicsWorld().addCollisionObject(object);
        }
        modelInstances.add(instance1.visualComponent.modelInstance);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //Camera logic
        cameraController3D.update();
        camera.update();

        //Process input
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {//rotate item
            rotationAngle += 90;
        }
        //Place item
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 position = getMouseGroundPosition(camera);
            //Spawn an instance of the item and add it to the physics simulation
            EntityInstance instance = spawner.spawnInstance();
            Matrix4 transform = new Matrix4().setToTranslation(position);
            transform.rotate(Vector3.Y, rotationAngle % 360);
            instance.place(transform);
            modelInstances.add(instance.visualComponent.modelInstance);
            for (btCollisionObject object : instance.entityPhysicsBodies) {
                physicsWorld.dynamicsWorld().addCollisionObject(object,
                    object instanceof btRigidBody ? CollisionRules.combineBits(CollisionRules.WORLD_GEOMETRY) : CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR));
            }
        }


        // Physics simulation Step
        physicsWorld.dynamicsWorld().stepSimulation(delta, 5, 1f / 240f);

        //Apply transforms to render models.
        for (int i = 0; i < modelInstances.size(); i++) {
            if (physicsWorld.dynamicsWorld().getCollisionObjectArray().atConst(i) instanceof btRigidBody body) {
                var motionState = body.getMotionState();
                if (motionState != null) {
                    motionState.getWorldTransform(modelInstances.get(i).transform);
                }
            }
        }


        //Render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
//        for (var instance : modelInstances) {
//            modelBatch.render(instance, environment);
//        }
        modelBatch.end();


        collisionManager.updateTouchingEntities();
        TimerUpdater.update(delta);

        physicsWorld.drawDebug(camera);

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
        btRigidBody.btRigidBodyConstructionInfo info =
            new btRigidBody.btRigidBodyConstructionInfo(0, motionState, shape, Vector3.Zero);
        btRigidBody body = new btRigidBody(info);
        info.dispose();
        return body;
    }

    private btRigidBody createDynamicBody(ModelInstance instance, btCollisionShape shape, float mass) {
        Vector3 inertia = new Vector3(0, 0, 0);
        shape.calculateLocalInertia(mass, inertia);
        Matrix4 transform = instance.transform;
        btDefaultMotionState motionState = new btDefaultMotionState(transform);
        btRigidBody.btRigidBodyConstructionInfo info =
            new btRigidBody.btRigidBodyConstructionInfo(mass, motionState, shape, inertia);
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
            if (intersection.y < 0f) intersection.y = 0f;
            return new Vector3(MathUtils.floor(intersection.x), MathUtils.floor(intersection.y), MathUtils.floor(intersection.z));
        } else {
            // No intersection (looking up at sky)
            return new Vector3(ray.origin);
        }
    }


}
