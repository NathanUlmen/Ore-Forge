package ore.forge.Strategies;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.EventSystem.Events.ItemRemovedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.DropperSpawner;
import ore.forge.Items.Experimental.EntityInstance;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.*;
import ore.forge.Strategies.DropperStrategies.BurstDrop;
import ore.forge.Strategies.DropperStrategies.DropStrategy;

import java.util.ArrayList;

public class DropOreBehavior implements Behavior, TimeUpdatable, GameEventListener<ItemRemovedGameEvent> {
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
        System.out.println("Transform in attach!");
        System.out.println(parent.getWorldTransform());
    }

    @Override
    public void update(float delta) {
//        if (dropperStrategy.drop(delta)) {
//            var body = GameWorld.instance().physicsWorld().createBody(oreDef);
//            body.createFixture(blueprint.fixtureDef);
//            Ore ore = OreRealm.getSingleton().giveOre();
//            ore.applyBaseStats(blueprint.oreValue, blueprint.oreTemperature, blueprint.multiOre, blueprint.name, "TESTING", null);
//            ore.setBody(body);
//            Vector2 dropperLocation = fixture.getBody().getPosition();
//            var itemData = fixture.getUserData();
//            Vector2 finalSpawnOffset = null;
//            if (itemData instanceof ItemUserData data) {
//                finalSpawnOffset = spawnOffset.rotateDeg(fixture.getBody().getAngle() + data.direction());
//            }
//            assert finalSpawnOffset != null;
//            body.setTransform(dropperLocation.x + finalSpawnOffset.x, dropperLocation.y + finalSpawnOffset.y, fixture.getBody().getAngle());
//
//        }

        //To Produce an ore we will need: OreModel, Ore Shape, and OreStats
        //This info will be taken from the DropperSpawner that this thing holds
        if (dropperStrategy.drop(delta)) {

            //Add ore to the world
            VisualComponent visualComponent = new VisualComponent(new ModelInstance(dropperSpawner.oreModel));
            Ore oreInfo = new Ore();

            Vector3 inertia = new Vector3();
            dropperSpawner.oreShape.calculateLocalInertia(10, inertia);
            var oreBody = new btRigidBody(10f, new btDefaultMotionState(), dropperSpawner.oreShape, inertia);
            oreBody.setSleepingThresholds(0, 0);
            oreBody.userData = oreInfo;
            oreInfo.rigidBody = oreBody;

            var collisionObjects = new ArrayList<btCollisionObject>();
            collisionObjects.add(oreBody);

            var oreInstance = new EntityInstance(oreInfo, collisionObjects, visualComponent);
            oreInstance.place(parent.getWorldTransform().cpy());
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
    public void colliding(Object subjectData, ItemUserData userData) {
        assert false;
    }

    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {

    }

    @Override
    public Behavior clone() {
        return new DropOreBehavior(this);
    }

    @Override
    public boolean isCollisionBehavior() {
        return false;
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
