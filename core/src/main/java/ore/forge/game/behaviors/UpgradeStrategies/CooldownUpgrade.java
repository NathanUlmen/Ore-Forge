package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;

/**
 * @author Nathan Ulmen
 */
@SuppressWarnings("unused")
public class CooldownUpgrade implements UpgradeStrategy {
    private final UpgradeStrategy upgrade;
    private final float cooldownTime;
    private float current;
    private float elapsedTime;

    public CooldownUpgrade(UpgradeStrategy upgrade, float cooldownTime) {
        this.upgrade = upgrade;
        this.cooldownTime = cooldownTime;
        this.current = 0;
    }

    private CooldownUpgrade(CooldownUpgrade clone) {
        this.upgrade = clone.upgrade.cloneUpgradeStrategy();
        this.cooldownTime = clone.cooldownTime;
        this.current = 0;
    }

    public CooldownUpgrade(JsonValue jsonValue) {
        this.upgrade = ReflectionLoader.load(jsonValue.get("upgrade"), "upgradeName");
        this.cooldownTime = jsonValue.getFloat("cooldownTime");
    }

    //TODO: make it so time is updated correctly.
    @Override
    public void applyTo(Ore ore) {
        if (current >= cooldownTime) {
            upgrade.applyTo(ore);
            current = 0;
            Gdx.app.log("CooldownUpgrade", "Cooldown upgrade Applied.");
        }
        Gdx.app.log("CooldownUpgrade", "Cooldown upgrade on Cooldown: " + current + "ms");
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return new CooldownUpgrade(this);
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + "] " +
            " cooldown Time: " + cooldownTime +
            " current time: " + current;
    }
}
