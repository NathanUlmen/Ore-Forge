package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.*;
import ore.forge.Expressions.Operands.NumericOreProperties;
import ore.forge.Expressions.Operators.NumericOperator;
import ore.forge.Input.CameraController3D;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderSpawner;
import ore.forge.Strategies.Behavior;
import ore.forge.Strategies.Move;
import ore.forge.Strategies.UpgradeBehavior;
import ore.forge.Strategies.UpgradeStrategies.BasicUpgrade;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

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
    private Batch batch;
    private btRigidBody cubeBody;

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
        t3.setToTranslation(0, 2f, 0);
        t3.rotate(1, 1, 1, 0f);
        cubeBody.setWorldTransform(t3);
        cubeBody.getMotionState().setWorldTransform(t3);

        //Initialize collision Manager
        collisionManager = new CollisionManager();

        JsonReader jsonReader = new JsonReader();
        JsonValue value = jsonReader.parse(Gdx.files.internal("Items/3DTestItem.json"));
        UpgraderSpawner spawner = new UpgraderSpawner(value);

        EntityInstance instance1 = spawner.createInstance();
        for (btCollisionObject object : instance1.entityPhysicsBodies) {
            physicsWorld.dynamicsWorld().addCollisionObject(object);
        }
        System.out.println("Created an EntityInstance!!");
        modelInstances.add(instance1.visualComponent.modelInstance);

//        createTestUpgrader();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // Physics and transforms
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


        //Camera logic
        cameraController3D.update();
        camera.update();


        //Render

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        for (var instance : modelInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();


        collisionManager.updateTouchingEntities();

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

    private void createTestUpgrader() {
        //Item Properties
        String itemName = "Basic Upgrader";
        String id = "abc123";
        String description = "An upgrader that adds 2 to the value of ore it upgrades";
        UpgradeTag upgradeTag = new UpgradeTag(itemName, id, 500, false);
        UpgradeStrategy upgradeStrategy = new BasicUpgrade(2, NumericOperator.ADD, NumericOreProperties.ORE_VALUE);
        UpgradeBehavior upgradeBehavior = new UpgradeBehavior(upgradeTag, upgradeStrategy, 0.5f);
        Behavior moveBehavior = new Move(.5f);

        //-----Item Bodies-----

        //---Create Conveyor---
        //Create View Model and Shape
        Model conveyorModel = createBox(20, 0.1f, 4, Color.GRAY);
        ModelInstance conveyorModelInstance = new ModelInstance(conveyorModel);
        Matrix4 conveyorTransform = new Matrix4();
        conveyorTransform.translate(0, 0.1f / 2f, 0);
        conveyorModelInstance.transform = conveyorTransform;
        modelInstances.add(conveyorModelInstance);
        modelInstances.add(conveyorModelInstance); //add second time to account for the conveyor ghost.

        //Create Physics Counterpart
        btCollisionShape collisionShape = new btBoxShape(new Vector3(10f, .05f, 2f)); //for a 4x4
        btRigidBody conveyorBody = createStaticBody(conveyorModelInstance, collisionShape);
        btGhostObject conveyorGhost = new btGhostObject();
        conveyorGhost.setCollisionShape(conveyorBody.getCollisionShape());
        conveyorGhost.setWorldTransform(conveyorTransform);
        conveyorGhost.userData = new ItemUserData(new Vector3(1, 0, 0), moveBehavior, null);


        //Add to physics simulation
        physicsWorld.dynamicsWorld().addRigidBody(conveyorBody);
        physicsWorld.dynamicsWorld().addCollisionObject(conveyorGhost);

        //---Upgrade Beam---
        //Create View Model and Shape
        Model beamModel = createBox(0.5f, .75f, 4, Color.BLUE);
        ModelInstance beamModelInstance = new ModelInstance(beamModel);
        Matrix4 beamTransform = new Matrix4();
        beamTransform.translate(0, .75f / 2f, 0); // center y = height/2
        beamModelInstance.transform.set(beamTransform);
        modelInstances.add(beamModelInstance);
        //Create Physics Counterpart
        btCollisionShape beamShape = new btBoxShape(new Vector3(0.5f / 2, 0.75f / 2, 4f / 2));
        btGhostObject beamGhost = new btGhostObject();
        beamGhost.setCollisionFlags(beamGhost.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        beamGhost.setCollisionShape(beamShape);
        beamGhost.setWorldTransform(beamTransform);
        beamGhost.userData = new ItemUserData(new Vector3(1, 0, 0), upgradeBehavior, null);
        //Add to physics simulation
        physicsWorld.dynamicsWorld().addCollisionObject(beamGhost);

        final float wallWidth = 4f;
        final float wallHeight = .75f;
        final float wallDepth = 1f;
        btCollisionShape wallShape = new btBoxShape(new Vector3(wallWidth / 2, wallHeight / 2, wallDepth / 2));

        //Wall 1
        Model wallModel = createBox(wallWidth, wallHeight, wallDepth, Color.RED);
        ModelInstance wallInstance1 = new ModelInstance(wallModel);
        Matrix4 wall1Transform = new Matrix4();
        wall1Transform.translate(0f, wallHeight / 2, -2.5f);
        wallInstance1.transform = wall1Transform;
        modelInstances.add(wallInstance1);
        btRigidBody wall1RigidBody = createStaticBody(wallInstance1, wallShape);

        //Wall 2
        Matrix4 wall2Transform = new Matrix4();
        ModelInstance wallInstance2 = new ModelInstance(wallModel);
        wall2Transform.translate(0f, wallHeight / 2, 2.5f);
        wallInstance2.transform = wall2Transform;
        modelInstances.add(wallInstance2);
        btRigidBody wall2RigidBody = createStaticBody(wallInstance2, wallShape);

        physicsWorld.dynamicsWorld().addRigidBody(wall1RigidBody);
        physicsWorld.dynamicsWorld().addRigidBody(wall2RigidBody);


    }


}
