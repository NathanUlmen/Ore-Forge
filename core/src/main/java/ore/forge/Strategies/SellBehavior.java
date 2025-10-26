package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.FurnaceBlueprint;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Ore;
import ore.forge.Player.Player;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

public class SellBehavior implements Behavior {
    private final UpgradeStrategy upgradeStrategy;
    private int spRewardProgress;
    private int spRewardThreshold, spRewardAmount;

    public SellBehavior(JsonValue value) {
        upgradeStrategy = ReflectionLoader.load(value.get("sellUpgrade"), "upgradeName");
        spRewardProgress = 0;
        spRewardThreshold = value.getInt("spRewardThreshold");
        spRewardAmount = value.getInt("spRewardAmount");
    }

    private SellBehavior(SellBehavior toClone) {
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

    @Override
    public void attach(Body body, Fixture fixture) {
        assert body.getUserData() instanceof FurnaceBlueprint;
        if (body.getUserData() instanceof FurnaceBlueprint bp) {
            this.spRewardAmount = bp.getSpRewardAmount();
            this.spRewardThreshold = bp.getSpRewardThreshold();
        }
    }

    @Override
    public void update(float delta) {
        assert false;
    }

    //TODO: UNFINISHED, events need to be handled
    @Override
    public void interact(Object subjectData, ItemUserData userData) {
//        if (contact.getUserData() instanceof Ore ore) {
//            upgradeStrategy.applyTo(ore);
//            var player = Player.getSingleton();
////            var eventManager = EventManager.getSingleton();
//            player.addToWallet(ore.getOreValue() * ore.getMultiOre());
////            eventManager.notifyListeners(new OreSoldGameEvent(ore, userData));
//
//            //Compute and Reward Special points
//            spRewardProgress += ore.getMultiOre();
//            player.addSpecialPoints(spRewardAmount * (spRewardProgress / spRewardThreshold));
//            spRewardProgress %= spRewardThreshold;
//
//            System.out.println("Ore sold for: " + ore.getOreValue() * ore.getMultiOre());
//
//            //TODO: Despawn ore
////            GameWorld.getInstance().physicsWorld().destroyBody(contact.getBody());
//
//        }
    }

    @Override
    public Behavior clone(Fixture parent) {
        return new SellBehavior(this);
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
    }

}
