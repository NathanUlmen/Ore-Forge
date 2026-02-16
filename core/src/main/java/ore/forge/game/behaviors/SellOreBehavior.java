package ore.forge.game.behaviors;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.engine.ReflectionLoader;
import ore.forge.game.CurrencyType;
import ore.forge.game.GameContext;
import ore.forge.game.Ore;
import ore.forge.game.PhysicsBodyData;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.behaviors.UpgradeStrategies.UpgradeStrategy;

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
    public void register(GameContext context) {

    }

    @Override
    public void unregister(GameContext context) {

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
//            eventManager.notifyListeners(new OreSoldGameEvent(ore, userData));

            //Compute and Reward Special points
            spRewardProgress += ore.getMultiOre();
            context.player.addCurrency(CurrencyType.SPECIAL_POINTS, spRewardAmount * (spRewardProgress / spRewardThreshold));
            spRewardProgress %= spRewardThreshold;

            context.entityManager.stageRemove(subject.parentEntityInstance);
        }
    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public BodyLogic clone() {
        return new SellOreBehavior(this);
    }

}
