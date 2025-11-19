package ore.forge.Strategies;


import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.UpgraderSpawner;
import ore.forge.Ore;
import ore.forge.ReflectionLoader;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;
import ore.forge.UpgradeCooldown;
import ore.forge.UpgradeTag;

@SuppressWarnings("unused")
public class UpgradeOreBehavior implements BodyLogic {
    private UpgradeTag upgradeTag;
    private final UpgradeStrategy upgradeStrategy;
    private final float cooldownDuration; //time before it can be upgraded in seconds.

    public UpgradeOreBehavior(JsonValue value) {
        upgradeStrategy = ReflectionLoader.load(value.get("upgrade"), "upgradeName");
        cooldownDuration = value.getFloat("cooldownDuration", 0.5f);
    }

    public UpgradeOreBehavior(UpgradeTag upgradeTag, UpgradeStrategy upgradeStrategy, float cooldownDuration) {
        this.upgradeTag = upgradeTag;
        this.upgradeStrategy = upgradeStrategy;
        this.cooldownDuration = cooldownDuration;
    }

    private UpgradeOreBehavior(UpgradeOreBehavior behavior) {
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
    public void attach(ItemSpawner spawner, btCollisionObject collisionObject) {
        assert spawner instanceof UpgraderSpawner;
        var upgraderSpawner = (UpgraderSpawner) spawner;
        this.upgradeTag = new UpgradeTag(upgraderSpawner.getUpgradeTag());
    }

    @Override
    public void update(float delta) {
        assert false;
    }

    //Upgrade the ore
    @Override
    public void onContactStart(Object subjectData, ItemUserData userData) {
        assert subjectData instanceof Ore;
        assert upgradeTag != null && upgradeStrategy != null;
        Ore ore = (Ore) subjectData;
        if (ore.isUpgradable(upgradeTag)) {
            upgradeStrategy.applyTo(ore);
//            System.out.println("Ore Upgraded!");
        }
    }

    @Override
    public void colliding(Object subjectData, ItemUserData userData) {
        //Upgrade ore if its upgradable (EX: UpgradeCooldown runs out while still touching)
    }

    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {
        //Start cooldown if not already in progress
        if (subjectData instanceof Ore ore && ore.isUpgradable(upgradeTag)) {
            ore.addUpgradeCooldown(upgradeTag, new UpgradeCooldown(cooldownDuration, ore, upgradeTag));
            ore.getUpgradeTag(upgradeTag).incrementCurrentUpgrades();
        }
    }

    @Override
    public BodyLogic clone() {
        return new UpgradeOreBehavior(this);
    }

}
