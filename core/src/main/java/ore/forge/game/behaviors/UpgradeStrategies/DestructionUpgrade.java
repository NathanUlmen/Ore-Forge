package ore.forge.game.behaviors.UpgradeStrategies;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;

@SuppressWarnings("unused")
public class DestructionUpgrade implements UpgradeStrategy {

    public DestructionUpgrade(JsonValue jsonValue) {
    }

    @Override
    public void applyTo(Ore ore) {
        ore.setIsDoomed(true);
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

}
