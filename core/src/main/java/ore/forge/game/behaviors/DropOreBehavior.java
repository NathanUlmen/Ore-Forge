package ore.forge.game.behaviors;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.EntityInstance;
import ore.forge.game.EntityInstanceCreator;
import ore.forge.engine.ReflectionLoader;
import ore.forge.game.*;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.items.Properties.DropperProperties;
import ore.forge.game.behaviors.DropperStrategies.BurstDrop;
import ore.forge.game.behaviors.DropperStrategies.DropStrategy;

@SuppressWarnings("unused")
public class DropOreBehavior implements BodyLogic, Updatable {
    private final DropStrategy dropperStrategy;
    private DropperProperties dropperProperties;
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
    public void register(GameContext context) {
        PhysicsBodyData data =  (PhysicsBodyData) parent.userData;
        data.parentEntityInstance.updatables.add(this);
        context.addUpdatable(this);
    }

    @Override
    public void unregister(GameContext context) {
        PhysicsBodyData data =  (PhysicsBodyData) parent.userData;
        data.parentEntityInstance.updatables.remove(this);
        context.removeUpdatable(this);
    }

    @Override
    public void attach(ItemDefinition definition, btCollisionObject parent) {
        dropperProperties = definition.itemProperties(DropperProperties.class);
        this.parent = parent;
    }

    @Override
    public void update(float delta, GameContext context) {
        //To Produce an ore we will need: OreModel, Ore Shape, and OreStats
        //This info will be taken from the DropperSpawner that this thing holds
        for (int i = 0; i < dropperStrategy.drop(delta); i++) {
//        if (dropperStrategy.drop(delta) > 0) {
            OreDefinition oreDefinition = dropperProperties.oreDefinition();
            EntityInstance ore = EntityInstanceCreator.createInstance(oreDefinition);
            ore.setTransform(this.parent.getWorldTransform().translate(0, -1f, 0));
            btRigidBody body = (btRigidBody) ore.physicsComponent.getBodies().getFirst().getRigidBody();
            body.setLinearVelocity(new Vector3(0, -20, 0));

            ore.updatables.add(new Updatable() {
                private final CoolDown cd = new CoolDown(2f);
                private boolean done = false;

                @Override
                public void update(float delta, GameContext context) {
                    if (!done && cd.update(delta) > 0) {
                        btRigidBody body = (btRigidBody) ore.physicsComponent
                            .getBodies()
                            .getFirst()
                            .getRigidBody();

                        btDynamicsWorld world = context.physicsWorld.dynamicsWorld();

                        world.removeRigidBody(body);

                        int newMask = CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR , CollisionRules.ORE, CollisionRules.WORLD_GEOMETRY);

                        world.addRigidBody(body, CollisionRules.combineBits(CollisionRules.ORE), newMask);

                        done = true;
                    }
                }
            });            //Add ore to the world
//            VisualComponent visualComponent = new VisualComponent(new ModelInstance(dropperProperties.oreModel));
//            Ore oreInfo = new Ore();


//            Vector3 inertia = new Vector3();
//            oreDefinition.oreShape().calculateLocalInertia(10, inertia);
//            btRigidBody oreBody = (btRigidBody) ore.physicsComponent.getRigidBody();
//            oreBody.setSleepingThresholds(1, 0);


//            oreBody.applyCentralImpulse(new Vector3(0, -150f, 0)); //Make it look like its "spitting" the ore out

//            oreBody.setSleepingThresholds(1, 1);
//            oreBody.setCcdMotionThreshold(0);
//            oreBody.setCcdMotionThreshold(0);

            context.entityManager.stageAdd(ore);
        }
    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public BodyLogic clone() {
        return new DropOreBehavior(this);
    }


    public record OreBlueprint(String name, double oreValue, float oreTemperature, int multiOre,
                               FixtureDef fixtureDef) {

    }

}
