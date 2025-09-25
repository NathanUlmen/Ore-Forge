package ore.forge.Strategies;


import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Ore;
import ore.forge.ReflectionLoader;
import ore.forge.Screens.CollisionBehavior;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;
import ore.forge.UpgradeCooldown;
import ore.forge.UpgradeTag;

@SuppressWarnings("unused")
public class UpgradeBehavior implements CollisionBehavior {
    private UpgradeTag upgradeTag;
    private final UpgradeStrategy upgradeStrategy;
    private final float cooldownDuration; //time before it can be upgraded in seconds.

    public UpgradeBehavior(JsonValue value) {
        upgradeStrategy = ReflectionLoader.load(value.get("upgrade"), "upgradeName");
        cooldownDuration = value.getFloat("cooldownDuration", 0.5f);
        this.upgradeTag = new UpgradeTag(value.parent.parent.get("upgradeTag"));
    }

    private UpgradeBehavior(UpgradeBehavior behavior) {
        this.upgradeTag = behavior.upgradeTag;
        this.upgradeStrategy = behavior.upgradeStrategy.cloneUpgradeStrategy();
        this.cooldownDuration = behavior.cooldownDuration;
    }

    @Override
    public void interact(Fixture contact, ItemBlueprint.ItemUserData userData) {
        if (contact.getUserData() instanceof Ore ore && ore.isUpgradable(upgradeTag)) {
            upgradeStrategy.applyTo(ore);
            ore.addUpgradeCooldown(upgradeTag, new UpgradeCooldown(cooldownDuration, ore, upgradeTag));
            ore.getUpgradeTag(upgradeTag).incrementCurrentUpgrades();
            System.out.println("Upgrade Complete");
        }
    }

    @Override
    public CollisionBehavior clone(Fixture parent) {
        return new UpgradeBehavior(this);
    }

}
