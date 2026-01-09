package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.GameContext;
import ore.forge.Items.ItemDefinition;
import ore.forge.Ore;
import ore.forge.PhysicsBodyData;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

public class SellOreBehavior implements BodyLogic {
    private final UpgradeStrategy upgradeStrategy;
    private int spRewardProgress;
    private int spRewardThreshold, spRewardAmount;

    public SellOreBehavior(JsonValue value) {
        upgradeStrategy = ReflectionLoader.load(value.get("sellUpgrade"), "upgradeName");
        spRewardProgress = 0;
        spRewardThreshold = value.getInt("spRewardThreshold");
        spRewardAmount = value.getInt("spRewardAmount");
    }

    private SellOreBehavior(SellOreBehavior toClone) {
        this.upgradeStrategy = toClone.upgradeStrategy.cloneUpgradeStrategy();
        this.spRewardProgress = 0;
        this.spRewardThreshold = toClone.spRewardThreshold;
        this.spRewardAmount = toClone.spRewardAmount;
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    public void attach(Body body, Fixture fixture) {
//        assert body.getUserData() instanceof FurnaceBlueprint;
//        if (body.getUserData() instanceof FurnaceBlueprint bp) {
//            this.spRewardAmount = bp.getSpRewardAmount();
//            this.spRewardThreshold = bp.getSpRewardThreshold();
//        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void attach(ItemDefinition definition, btCollisionObject parent) {

    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching) {
        if (subject.specificData instanceof Ore ore && timeTouching > 0.5f) {
            upgradeStrategy.applyTo(ore);
//            var player = ore.forge.Player.Player.getSingleton();
//            var eventManager = EventManager.getSingleton();
//            player.addToWallet(ore.getOreValue() * ore.getMultiOre());
//            eventManager.notifyListeners(new OreSoldGameEvent(ore, userData));

            //Compute and Reward Special points
//            spRewardProgress += ore.getMultiOre();
//            player.addSpecialPoints(spRewardAmount * (spRewardProgress / spRewardThreshold));
//            spRewardProgress %= spRewardThreshold;


            /*
             * TODO: Despawn Ore
             * Remove from touching objects safely
             * Remove form physics Simulation
             * Remove from render list
             *
             * */
//            ore.rigidBody.setWorldTransform(ore.rigidBody.getWorldTransform().translate(0, 10000, 0));
//            PhysicsWorld.instance().dynamicsWorld().removeRigidBody(ore.rigidBody);
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public BodyLogic clone() {
        return new SellOreBehavior(this);
    }

}
