package ore.forge.Strategies;


import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.*;
import ore.forge.Items.ItemDefinition;
import ore.forge.Items.Properties.UpgraderProperties;
import ore.forge.Strategies.UpgradeStrategies.UpgradeStrategy;

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
    public void register(GameContext context) {

    }

    @Override
    public void unregister(GameContext context) {

    }

    @Override
    public void attach(ItemDefinition definition, btCollisionObject collisionObject) {
        UpgraderProperties properties = definition.itemProperties(UpgraderProperties.class);
        this.upgradeTag = new UpgradeTag(properties.upgradeTag());
    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {
        assert subject.specificData instanceof Ore;
        assert upgradeTag != null && upgradeStrategy != null;
        Ore ore = (Ore) subject.specificData;
        if (ore.isUpgradable(upgradeTag)) {
            upgradeStrategy.applyTo(ore);
//            System.out.println("Ore Upgraded!");
        }
    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {
        if (subject.specificData instanceof Ore ore && ore.isUpgradable(upgradeTag)) {
            ore.addUpgradeCooldown(upgradeTag, new UpgradeCooldown(cooldownDuration, ore, upgradeTag));
            ore.getUpgradeTag(upgradeTag).incrementCurrentUpgrades();
        }
    }

    @Override
    public BodyLogic clone() {
        return new UpgradeOreBehavior(this);
    }

}
