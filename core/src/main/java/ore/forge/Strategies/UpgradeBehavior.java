package ore.forge.Strategies;


import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderBlueprint;
import ore.forge.Items.Experimental.UpgraderSpawner;
import ore.forge.Ore;
import ore.forge.ReflectionLoader;
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
    public void attach(UpgraderSpawner spawner, btCollisionObject collisionObject) {
        assert spawner instanceof UpgraderSpawner;
        this.upgradeTag = new UpgradeTag(spawner.getUpgradeTag());
    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void onContactStart(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void colliding(Object subjectData, ItemUserData userData) {
        assert subjectData instanceof Ore;
        assert upgradeTag != null && upgradeStrategy != null;
        if (subjectData instanceof Ore ore && ore.isUpgradable(upgradeTag)) {
            upgradeStrategy.applyTo(ore);
            ore.addUpgradeCooldown(upgradeTag, new UpgradeCooldown(cooldownDuration, ore, upgradeTag));
            ore.getUpgradeTag(upgradeTag).incrementCurrentUpgrades();
            System.out.println("Upgrade Complete");
        }
    }

    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {

    }

    @Override
    public Behavior clone() {
        return new UpgradeBehavior(this);
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
    }

}
