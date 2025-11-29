package ore.forge.Strategies;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.*;
import ore.forge.EventSystem.Events.ItemRemovedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.DropperSpawner;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Screens.Gameplay3D;
import ore.forge.Strategies.DropperStrategies.BurstDrop;
import ore.forge.Strategies.DropperStrategies.DropStrategy;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class DropOreBehavior implements BodyLogic, TimeUpdatable, GameEventListener<ItemRemovedGameEvent> {
    private final DropStrategy dropperStrategy;
    private DropperSpawner dropperSpawner;
    private btCollisionObject parent;

    public DropOreBehavior(JsonValue value) {
        dropperStrategy = ReflectionLoader.load(value.get("dropBehavior"), "dropBehaviorName"); //TODO: field name

//        value = value.parent.parent.get("oreProperties");
//        String oreName = value.getString("oreName");
//        double oreValue = value.getDouble("oreValue");
//        float oreTemperature = value.getFloat("oreTemperature");
//        int multiOre = value.getInt("multiOre");
    }

    private DropOreBehavior(DropOreBehavior toClone) {
        this.dropperStrategy = new BurstDrop((BurstDrop) toClone.dropperStrategy);
    }

    @Override
    public void register() {
        TimerUpdater.register(this);
    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(ItemSpawner spawner, btCollisionObject parent) {
        this.dropperSpawner = (DropperSpawner) spawner;
        this.parent = parent;
    }

    @Override
    public void update(float delta) {
        //To Produce an ore we will need: OreModel, Ore Shape, and OreStats
        //This info will be taken from the DropperSpawner that this thing holds
        if (dropperStrategy.drop(delta)) {

            //Add ore to the world
            VisualComponent visualComponent = new VisualComponent(new ModelInstance(dropperSpawner.oreModel));
            Ore oreInfo = new Ore();


            Vector3 inertia = new Vector3();
            dropperSpawner.oreShape.calculateLocalInertia(10, inertia);
            var oreBody = new btRigidBody(10f, new btDefaultMotionState(), dropperSpawner.oreShape, inertia);
//            oreBody.setSleepingThresholds(1, 0);

            oreInfo.rigidBody = oreBody;
            oreBody.applyCentralImpulse(new Vector3(0, -150f, 0)); //Make it look like its "spitting" the ore out

            var collisionObjects = new ArrayList<btCollisionObject>();
            collisionObjects.add(oreBody);
            oreBody.setSleepingThresholds(1, 1);
            oreBody.setCcdMotionThreshold(0);
            oreBody.setCcdMotionThreshold(0);

            var oreInstance = new EntityInstance(collisionObjects, visualComponent);
            oreBody.userData = new PhysicsBodyData(oreInstance, oreInfo, null, oreBody.getWorldTransform());

            oreInstance.place(parent.getWorldTransform().cpy());
            Gameplay3D.modelInstances.add(oreInstance.visualComponent.modelInstance);
            Gameplay3D.entityInstances.add(oreInstance);
            for (var object : oreInstance.entityPhysicsBodies) {
                PhysicsWorld.instance().dynamicsWorld().addRigidBody((btRigidBody) object,
                    CollisionRules.combineBits(CollisionRules.ORE),
                    CollisionRules.combineBits(CollisionRules.ORE, CollisionRules.ORE_PROCESSOR, CollisionRules.WORLD_GEOMETRY));
            }
        }
    }

    @Override
    public void onContactStart(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    @Override
    public void colliding(Object subjectData, ItemUserData userData) {
        assert false;
    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, float timeTouching) {

    }

    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    @Override
    public BodyLogic clone() {
        return new DropOreBehavior(this);
    }

    @Override
    public void handle(ItemRemovedGameEvent event) {

    }

    @Override
    public Class<?> getEventType() {
        return ItemRemovedGameEvent.class;
    }

    public record OreBlueprint(String name, double oreValue, float oreTemperature, int multiOre,
                               FixtureDef fixtureDef) {

    }

}
