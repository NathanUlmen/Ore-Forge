package ore.forge.game.behaviors.UpgradeStrategies;


import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;

//Resets all nonResetterTags on an ore.
@SuppressWarnings("unused")
public class ResetterUpgrade implements UpgradeStrategy {

    public ResetterUpgrade() {
    }

    public ResetterUpgrade(JsonValue jsonValue) {

    }

    @Override
    public void applyTo(Ore ore) {
        ore.resetNonResetterTags();
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
