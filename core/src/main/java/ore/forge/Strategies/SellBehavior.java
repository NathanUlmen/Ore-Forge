package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.EventSystem.EventManager;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Ore;
import ore.forge.Player.Player;
import ore.forge.ReflectionLoader;
import ore.forge.Screens.CollisionBehavior;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

public class SellBehavior implements CollisionBehavior {
    private final UpgradeStrategy upgradeStrategy;
    private int spRewardProgress;
    private final int spRewardThreshold, spRewardAmount;

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

    //TODO: UNFINISHED, events need to be handled
    @Override
    public void interact(Fixture contact, ItemBlueprint.ItemUserData userData) {
        if (contact.getUserData() instanceof Ore ore) {
            upgradeStrategy.applyTo(ore);
            var player = Player.getSingleton();
            var eventManager = EventManager.getSingleton();
            player.addToWallet(ore.getOreValue() * ore.getMultiOre());
//            eventManager.notifyListeners(new OreSoldGameEvent(ore, userData));

            //Compute and Reward Special points
            spRewardProgress += ore.getMultiOre();
            player.addSpecialPoints(spRewardAmount * (spRewardProgress / spRewardThreshold));
            spRewardProgress %= spRewardThreshold;

            System.out.println("Ore sold for: " + ore.getOreValue() * ore.getMultiOre());

            //TODO: Despawn ore
//            GameWorld.getInstance().physicsWorld().destroyBody(contact.getBody());

        }
    }

    @Override
    public CollisionBehavior clone(Fixture parent) {
        return new SellBehavior(this);
    }

}
