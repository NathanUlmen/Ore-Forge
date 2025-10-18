package ore.forge.Strategies;


import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderBlueprint;
import ore.forge.Ore;
import ore.forge.ReflectionLoader;
import ore.forge.Screens.Behavior;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;
import ore.forge.UpgradeCooldown;
import ore.forge.UpgradeTag;

@SuppressWarnings("unused")
public class UpgradeBehavior implements Behavior {
    private UpgradeTag upgradeTag;
    private final UpgradeStrategy upgradeStrategy;
    private final float cooldownDuration; //time before it can be upgraded in seconds.

    public UpgradeBehavior(JsonValue value) {
        upgradeStrategy = ReflectionLoader.load(value.get("upgrade"), "upgradeName");
        cooldownDuration = value.getFloat("cooldownDuration", 0.5f);
    }

    public UpgradeBehavior(UpgradeTag upgradeTag, UpgradeStrategy upgradeStrategy, float cooldownDuration) {
        this.upgradeTag = upgradeTag;
        this.upgradeStrategy = upgradeStrategy;
        this.cooldownDuration = cooldownDuration;
    }

    private UpgradeBehavior(UpgradeBehavior behavior) {
        this.upgradeTag = behavior.upgradeTag;
        this.upgradeStrategy = behavior.upgradeStrategy.cloneUpgradeStrategy();
        this.cooldownDuration = behavior.cooldownDuration;
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(Body body, Fixture fixture) {
        assert body.getUserData() instanceof UpgraderBlueprint bp;
        if (body.getUserData() instanceof  UpgraderBlueprint bp) {
            this.upgradeTag = new UpgradeTag(bp.getUpgradeTag());
        }
    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void interact(Fixture contact, ItemUserData userData) {
        assert upgradeTag != null && upgradeStrategy != null;
        if (contact.getUserData() instanceof Ore ore && ore.isUpgradable(upgradeTag)) {
            upgradeStrategy.applyTo(ore);
            ore.addUpgradeCooldown(upgradeTag, new UpgradeCooldown(cooldownDuration, ore, upgradeTag));
            ore.getUpgradeTag(upgradeTag).incrementCurrentUpgrades();
            System.out.println("Upgrade Complete");
        }
    }

    @Override
    public Behavior clone(Fixture parent) {
        return new UpgradeBehavior(this);
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
    }

}
