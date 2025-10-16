package ore.forge.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import ore.forge.Input.CameraController3D;

import java.util.ArrayList;

public class Gameplay3D implements Screen {
    //Rendering
    private final PerspectiveCamera camera;
    private final ModelBatch modelBatch;
    private final Environment environment;
    private final CameraController3D cameraController3D;
    private final ArrayList<ModelInstance> modelInstances;

    //Physics
    btDiscreteDynamicsWorld dynamicsWorld;
    btDispatcher dispatcher;
    btBroadphaseInterface broadphase;
    btConstraintSolver solver;
    btCollisionConfiguration collisionConfig;

    public Gameplay3D() {
        //Config camera
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 1f; //Min distance can see/draw
        camera.far = 1000f; //Max distance can see/draw
        camera.position.set(40, 20, 20);
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
        Model model = createBox(5, 5, 5, Color.PINK);
        var instance = new ModelInstance(model);
        transform.translate(0, 2.5f, 0);
        instance.transform.set(transform);
        modelInstances.add(instance);

        //create plane and apply transform
        instance = new ModelInstance(createBox(50, 0.01f, 50));
        var t2 = new Matrix4();
        t2.translate(0, 0, 0);
        instance.transform.set(t2);
        modelInstances.add(instance);

        //Config Physics
        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();

        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -9.81f, 0));

        // Create collision shapes
        btCollisionShape boxShape = new btBoxShape(new Vector3(2.5f, 2.5f, 2.5f));
        btCollisionShape planeShape = new btBoxShape(new Vector3(25f, 0.01f, 25f));

        // Create rigid bodies
        btRigidBody cubeBody = createDynamicBody(modelInstances.get(0), boxShape, 10f);
        btRigidBody groundBody = createStaticBody(modelInstances.get(1), planeShape);


        // Add to world
        dynamicsWorld.addRigidBody(cubeBody);
        dynamicsWorld.addRigidBody(groundBody);

        Matrix4 t3 = new Matrix4();
        cubeBody.getWorldTransform(t3);
        t3.setToTranslation(0, 20f, 0);
        t3.rotate(1, 1, 1, 70f);
        cubeBody.setWorldTransform(t3);
        cubeBody.getMotionState().setWorldTransform(t3);


    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        //update cube transform
        btRigidBody cubeBody = (btRigidBody) dynamicsWorld.getCollisionObjectArray().atConst(0);
        btMotionState motionState = cubeBody.getMotionState();
        if (motionState != null) {
            motionState.getWorldTransform(modelInstances.get(0).transform);
        }

        //lock camera onto cube if its moving
        if (cubeBody.getLinearVelocity().len2() > 0.0001f) {
            Vector3 cubePos = new Vector3();
            modelInstances.get(0).transform.getTranslation(cubePos);
            Vector3 desiredDir = cubePos.cpy().sub(camera.position).nor();
            camera.direction.lerp(desiredDir, delta * 2f).nor();
        } else {
            cameraController3D.update();
        }

        camera.update();

        //updarte physics
        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        camera.update();
        modelBatch.begin(camera);
        for (var instance : modelInstances) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();
        System.out.println(camera.direction);
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


}
