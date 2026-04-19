package ore.forge.game.temp;

import com.badlogic.gdx.Screen;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import ore.forge.engine.PhysicsBodyType;
import ore.forge.engine.PhysicsMotionType;
import ore.forge.engine.PhysicsWorld;
import ore.forge.engine.components.*;
import ore.forge.engine.render.*;
import ore.forge.engine.systems.*;
import ore.forge.game.GameContext2;
import ore.forge.game.Tickable;
import ore.forge.game.UpdatableScriptC;
import ore.forge.game.behaviors.DropperStrategies.BurstDrop;
import ore.forge.game.behaviors.DropperStrategies.DropStrategy;
import ore.forge.game.collisions.*;
import ore.forge.game.components.Ore;
import ore.forge.game.input.FreeCamController;

import java.util.ArrayList;
import java.util.List;

import static ore.forge.game.GameContext2.FIXED_TIME_STEP;
import static ore.forge.game.GameContext2.MAX_SUBSTEPS;

public class TestItemEntity implements Screen {
    private final Engine engine = new Engine();
    private final Renderer renderer = new Renderer();
    private final AssetHandler assetHandler = new AssetHandler();
    private final PhysicsWorld physicsWorld = PhysicsWorld.instance();
    private final CollisionManager collisionManager = new CollisionManager();
    private final FreeCamController controller;
    private final MaterialHandle materialHandle;
    private float accumulator = 0f;

    public TestItemEntity() {
        initSystems();
        PerspectiveCamera camera = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 0f, 60f); // pull back so you can see the grid
        camera.lookAt(0f, 0f, 0f);
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.up.set(0f, 1f, 0f);
        camera.update(true);

        controller = new FreeCamController(camera);


        renderer.addRenderPass(new BasicRenderPass());
        materialHandle = new MaterialHandle();
        materialHandle.shader = renderer.renderPasses().getFirst().currentShader;
//        createFloor();
        createDropper();
        createCompoundWithChildRenderPieces();
    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        controller.update(delta);
        controller.getCamera().update();

        accumulator += delta;
        while (accumulator >= FIXED_TIME_STEP) {
            float dt = FIXED_TIME_STEP;
            //pre sync transforms
            var preTickSync = engine.getSystem(PrePhysicsTransformSyncSystem.class);
            preTickSync.update(dt);

            //step Physics
            physicsWorld.dynamicsWorld().stepSimulation(dt, MAX_SUBSTEPS, FIXED_TIME_STEP);

            //update contacts
            collisionManager.update(dt);

            //resolve callbacks
            var physicsCallbackResolver = engine.getSystem(PhysicsCallbackResolverSystem.class);
            physicsCallbackResolver.update(dt);

            //updatables
            var updatableSystem = engine.getSystem(UpdatableSystem.class);
            updatableSystem.update(dt);

            var teleportSystem = engine.getSystem(TeleportSystem.class);
            teleportSystem.update(dt);

            var foo = engine.getSystem(TransformHierarchySystem.class);
            foo.update(dt);

            //sync after physics
            var postTickSync = engine.getSystem(PostPhysicsTransformSyncSystem.class);
            postTickSync.update(dt);
            accumulator -= FIXED_TIME_STEP;


        }

        float alpha = accumulator / FIXED_TIME_STEP;
        //prepareRender
        var prepareRender = engine.getSystem(RenderPrepSystem.class);
        prepareRender.setAlpha(alpha);
        prepareRender.update(delta);


        List<RenderPart> toRender = new ArrayList<>(1024);
        for (Entity entity : engine.getEntitiesFor(Family.all(RenderC.class).get())) {
            var renderC = entity.getComponent(RenderC.class);
            toRender.add(renderC.renderPart);
        }
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        renderer.render(toRender, controller.getCamera());

//        physicsWorld.drawDebug(controller.getCamera());

//        physicsWorld.drawDebug(controller.getCamera());
//        System.out.println(Gdx.graphics.getFramesPerSecond());
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

    private void initSystems() {
        engine.addSystem(new PrePhysicsTransformSyncSystem());
        engine.addSystem(new PhysicsCallbackResolverSystem(new GameContext2(), collisionManager));
        engine.addSystem(new UpdatableSystem());
        engine.addSystem(new TeleportSystem());
        engine.addSystem(new RenderPrepSystem());
        engine.addSystem(new PostPhysicsTransformSyncSystem());
        engine.addSystem(new TransformHierarchySystem());
    }

    private void createDropper() {
        Entity dropper = engine.createEntity();

        //Setup Physics
        PhysicsC physicsC = (PhysicsC) dropper.addAndReturn(engine.createComponent(PhysicsC.class));
        btCollisionShape collisionShape = new btBoxShape(new Vector3(4, 4, 4));
        physicsC.collisionObject = new btRigidBody(0, new btDefaultMotionState(),  collisionShape);
        physicsC.motionType = PhysicsMotionType.KINEMATIC;

        TransformC transformC = (TransformC) dropper.addAndReturn(engine.createComponent(TransformC.class));
        transformC.setBothLocal(new Matrix4());

        WorldTransformC worldTransformC = (WorldTransformC) dropper.addAndReturn(engine.createComponent(WorldTransformC.class));

        RenderC renderC = (RenderC) dropper.addAndReturn(engine.createComponent(RenderC.class));
        renderC.renderPart = RenderPart.defaultRenderPart(assetHandler.getHandle("Cube"));
        renderC.scale.set(4, 4, 4);
        renderC.renderPart.material = materialHandle;

        UpdatableScriptC updatableScriptC = (UpdatableScriptC) dropper.addAndReturn(engine.createComponent(UpdatableScriptC.class));
        updatableScriptC.add(new Tickable() {
            btCollisionShape collisionShape = new btBoxShape(new Vector3(.5f, .15f, .4f));
            Vector3 offset = new Vector3(0, 2, 2.5f * 2);
            Vector3 tmp = new Vector3();
            DropStrategy dropStrategy = new BurstDrop(90, 1);

            @Override
            public void update(float delta, GameContext2 gameContext, Entity entity) {
                for (int i = 0; i < dropStrategy.drop(delta); i++) {
                    System.out.println("Dropped!");
                    Entity oreEntity = engine.createEntity();

                    PhysicsC physicsC = (PhysicsC) oreEntity.addAndReturn(engine.createComponent(PhysicsC.class));
                    physicsC.motionType = PhysicsMotionType.DYNAMIC;
                    Vector3 inertia = new Vector3();
                    collisionShape.calculateLocalInertia(10, inertia);
                    physicsC.collisionObject = new btRigidBody(10, new btDefaultMotionState(),  collisionShape, inertia);
                    physicsC.collisionObject.userData = oreEntity;
                    physicsC.motionType =  PhysicsMotionType.DYNAMIC;
                    physicsC.bodyType = PhysicsBodyType.RIGID;
                    physicsC.collisionObject.setCollisionFlags(physicsC.collisionObject.getCollisionFlags()
                        | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                    physicsWorld.dynamicsWorld().addRigidBody((btRigidBody) physicsC.collisionObject);

                    RenderC renderC = (RenderC) oreEntity.addAndReturn(engine.createComponent(RenderC.class));
                    renderC.renderPart = RenderPart.defaultRenderPart(assetHandler.getHandle("Emerald"));
                    renderC.scale.set(.5f, .5f, .5f);
                    renderC.renderPart.material = materialHandle;

                    TransformC transformC = (TransformC) oreEntity.addAndReturn(engine.createComponent(TransformC.class));


                    Ore ore = (Ore) oreEntity.addAndReturn(engine.createComponent(Ore.class));

                    oreEntity.add(IdComponent.create());

                    WorldTransformC wt = new WorldTransformC();
                    oreEntity.add(wt);

                    TeleportRequestC teleportRequestC = new TeleportRequestC();
                    oreEntity.add(teleportRequestC);
                    //Spawn position
                    TransformC dropperTransform = entity.getComponent(TransformC.class);
                    tmp.set(dropperTransform.localPosition);
                    tmp.add(offset);
                    transformC.setBothLocal(new Matrix4().setTranslation(tmp));
                    teleportRequestC.targetRootWorld.setToTranslation(tmp);
                    engine.addEntity(oreEntity);
                }
            }
        });

        engine.addEntity(dropper);
    }

    public void createFloor() {
        Entity floor = engine.createEntity();
        engine.addEntity(floor);

        WorldTransformC worldTransformC = (WorldTransformC) floor.addAndReturn(engine.createComponent(WorldTransformC.class));
        worldTransformC.setBoth(new Matrix4().translate(0, -0.5f, 0));

        TransformC transformC = (TransformC) floor.addAndReturn(engine.createComponent(TransformC.class));
        transformC.setBothLocal(new Matrix4());

        floor.add(IdComponent.create());

        PhysicsC physicsC = (PhysicsC) floor.addAndReturn(engine.createComponent(PhysicsC.class));
        physicsC.collisionObject = new btRigidBody(0, new btDefaultMotionState(), new btBoxShape(new Vector3(25, 0.5f, 25)));
        physicsWorld.dynamicsWorld().addCollisionObject(physicsC.collisionObject);
        physicsC.motionType = PhysicsMotionType.STATIC;

        RenderC renderC = (RenderC) floor.addAndReturn(engine.createComponent(RenderC.class));
        renderC.renderPart = RenderPart.defaultRenderPart(assetHandler.getHandle("Cube"));
        renderC.scale.set(25, .5f, 25);
        renderC.localFromEntity.translate(0, 0.5f, 0);
        renderC.renderPart.material = materialHandle;
    }

    private void createCompoundWithChildRenderPieces() {
        Entity upgrader = engine.createEntity();

        upgrader.add(IdComponent.create());

        // --- Physics (compound) ---
        PhysicsC physicsC = (PhysicsC) upgrader.addAndReturn(engine.createComponent(PhysicsC.class));
        upgrader.add(IdComponent.create());

        btCompoundShape compound = new btCompoundShape();
        Matrix4 childM4 = new Matrix4();
        Quaternion q = new Quaternion();

        // Root rigid body (static/kinematic)
        physicsC.collisionObject = new btRigidBody(0f, new btDefaultMotionState(), compound);
        physicsC.motionType = PhysicsMotionType.KINEMATIC;
        physicsWorld.dynamicsWorld().addCollisionObject(physicsC.collisionObject);
        physicsC.collisionObject.userData = upgrader;
        physicsC.collisionObject.setCollisionFlags(physicsC.collisionObject.getCollisionFlags()
            | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);


        CollisionHandlerC handlerC = (CollisionHandlerC) upgrader.addAndReturn(engine.createComponent(CollisionHandlerC.class));

        // --- Root transforms / parenting ---
        TransformC rootTransformC = (TransformC) upgrader.addAndReturn(engine.createComponent(TransformC.class));
        rootTransformC.setBothLocal(new Matrix4());

        WorldTransformC rootWorldTransformC = (WorldTransformC) upgrader.addAndReturn(engine.createComponent(WorldTransformC.class));

        ParentC parentC = (ParentC) upgrader.addAndReturn(engine.createComponent(ParentC.class));

        //Conveyor part
        addPart(upgrader, compound, childM4, q,
            parentC, "Cube",
            40f, 0.1f, 4f,
            0f, 0f, 0f,
            0f, 0f, -35f);
        CollisionHandlerC.HandlerSet handlerSet = new CollisionHandlerC.HandlerSet();
        handlerSet.touchings.add(new OnContactTouching() {
            final Vector3 dirOffset = new Vector3(-1, 0,0);
            final float maxSpeed = 200f;
            @Override
            public void onTouching(Entity self, Entity other, CollisionEvent e, GameContext2 context) {
                PhysicsC otherPhysics = other.getComponent(PhysicsC.class);
                if (otherPhysics.collisionObject instanceof btRigidBody rigidBody) {
//                DirectionC selfDir = (DirectionC) self.addAndReturn(engine.createComponent(DirectionC.class));
                    Vector3 bodyVelocity = rigidBody.getLinearVelocity();

                    float velocityOnDir = bodyVelocity.cpy().dot(dirOffset);

                    float velocityChange = maxSpeed -  velocityOnDir;

                    Vector3 forceVec = dirOffset.cpy().scl(velocityChange);

                    rigidBody.applyCentralForce(forceVec);
                }
            }
        });
        handlerSet.starts.add(new OnContactStart() {
            final Vector3 dirOffset = new Vector3(-1, 0,0);
            final float maxSpeed = 200f;
            @Override
            public void onStart(Entity self, Entity other, CollisionEvent e, GameContext2 context) {
                PhysicsC otherPhysics = other.getComponent(PhysicsC.class);
                if (otherPhysics.collisionObject instanceof btRigidBody rigidBody) {
//                DirectionC selfDir = (DirectionC) self.addAndReturn(engine.createComponent(DirectionC.class));
                    Vector3 bodyVelocity = rigidBody.getLinearVelocity();

                    float velocityOnDir = bodyVelocity.cpy().dot(dirOffset);

                    float velocityChange = maxSpeed -  velocityOnDir;

                    Vector3 forceVec = dirOffset.cpy().scl(velocityChange);

                    rigidBody.applyCentralForce(forceVec);
                }
            }
        });
        handlerC.perChild.put(0, handlerSet);

        //Upgrade Beam
        addPartWithPhysics(upgrader, compound, childM4, q,
            parentC, "Cube",
            0.5f, 2f, 4f,
            0f, 1f, 0f,
            0f, 0f, 0f);
//        CollisionHandlerC.HandlerSet handlerSet2 = new CollisionHandlerC.HandlerSet();
//        handlerSet2.starts.add(new OnContactStart() {
//            @Override
//            public void onStart(Entity self, Entity other, CollisionEvent e, GameContext2 context) {
//                Ore ore = other.getComponent(Ore.class);
//                ore.setOreValue(ore.getOreValue() + 2);
//                System.out.println("Ore Value: " + ore.getOreValue());
//            }
//        });
//        handlerC.perChild.put(1, handlerSet2);

        //Wall
        addPart(upgrader, compound, childM4, q,
            parentC, "Cube",
            4f, 0.75f, 1f,
            0f, 0f, -2.5f,
            0f, 0f, 0f);

        //Wall
        addPart(upgrader, compound, childM4, q,
            parentC, "Cube",
            4f, 0.75f, 1f,
            0f, 0f, 2.5f,
            0f, 0f, 0f);

        engine.addEntity(upgrader);
        TeleportRequestC requestC = (TeleportRequestC) upgrader.addAndReturn(engine.createComponent(TeleportRequestC.class));
        requestC.targetRootWorld.setToTranslation(-0, -2, 5);
    }

    private void addPart(Entity parent, btCompoundShape compound,
                         Matrix4 childM4, Quaternion q,
                         ParentC parentC,
                         String meshHandleName,
                         float dimX, float dimY, float dimZ,
                         float posX, float posY, float posZ,
                         float rotX, float rotY, float rotZ) {

        // --- Physics child ---
        btCollisionShape shape = new btBoxShape(new Vector3(dimX * 0.5f, dimY * 0.5f, dimZ * 0.5f));

        q.idt().setEulerAngles(rotY, rotX, rotZ); // yaw(Y), pitch(X), roll(Z)
        childM4.idt().set(q).setTranslation(posX, posY, posZ);

        compound.addChildShape(childM4, shape);

        // --- Render child entity ---
        Entity child = engine.createEntity();

        ChildC childC = (ChildC) child.addAndReturn(engine.createComponent(ChildC.class));
        childC.parent = parent;

        // Local transform relative to parent
        TransformC childTransformC = (TransformC) child.addAndReturn(engine.createComponent(TransformC.class));
        childTransformC.setBothLocal(childM4); // copy: position + rotation

        WorldTransformC childWorldTransformC = (WorldTransformC) child.addAndReturn(engine.createComponent(WorldTransformC.class));

        RenderC childRenderC = (RenderC) child.addAndReturn(engine.createComponent(RenderC.class));

        childRenderC.renderPart = RenderPart.defaultRenderPart(assetHandler.getHandle(meshHandleName));
        childRenderC.renderPart.material = materialHandle;

        // Visual size: FULL extents (matches the JSON dimensions)
        childRenderC.scale.set(dimX/2, dimY/2, dimZ/2);

        // Parent it
        parentC.add(child);

        // Depending on your engine, ParentC.add(child) might not auto-add to engine.
        // If you need it explicit, uncomment:
        engine.addEntity(child);
    }

    private void addPartWithPhysics(Entity parent, btCompoundShape compound,
                                    Matrix4 childM4, Quaternion q,
                                    ParentC parentC,
                                    String meshHandleName,
                                    float dimX, float dimY, float dimZ,
                                    float posX, float posY, float posZ,
                                    float rotX, float rotY, float rotZ) {

        // --- Physics child ---
        btCollisionShape shape = new btBoxShape(new Vector3(dimX * 0.5f, dimY * 0.5f, dimZ * 0.5f));

        q.idt().setEulerAngles(rotY, rotX, rotZ); // yaw(Y), pitch(X), roll(Z)
        childM4.idt().set(q).setTranslation(posX, posY, posZ);


        // --- Render child entity ---
        Entity child = engine.createEntity();

        child.add(IdComponent.create());

        ChildC childC = (ChildC) child.addAndReturn(engine.createComponent(ChildC.class));
        childC.parent = parent;

        PhysicsC physicsC = (PhysicsC) child.addAndReturn(engine.createComponent(PhysicsC.class));
        physicsC.collisionObject = new btGhostObject();
        physicsC.collisionObject.setCollisionShape(shape);
        physicsC.collisionObject.setCollisionFlags(physicsC.collisionObject.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_NO_CONTACT_RESPONSE);
        physicsWorld.dynamicsWorld().addCollisionObject(physicsC.collisionObject);
        physicsC.motionType = PhysicsMotionType.KINEMATIC;


        // Local transform relative to parent
        TransformC childTransformC = (TransformC) child.addAndReturn(engine.createComponent(TransformC.class));
        childTransformC.setBothLocal(childM4); // copy: position + rotation

        WorldTransformC childWorldTransformC = (WorldTransformC) child.addAndReturn(engine.createComponent(WorldTransformC.class));

        RenderC childRenderC = (RenderC) child.addAndReturn(engine.createComponent(RenderC.class));

        childRenderC.renderPart = RenderPart.defaultRenderPart(assetHandler.getHandle(meshHandleName));
        childRenderC.renderPart.material = materialHandle;

        // Visual size: FULL extents (matches the JSON dimensions)
        childRenderC.scale.set(dimX/2, dimY/2, dimZ/2);

        // Parent it
        parentC.add(child);

        // Depending on your engine, ParentC.add(child) might not auto-add to engine.
        // If you need it explicit, uncomment:
        engine.addEntity(child);
    }

}

