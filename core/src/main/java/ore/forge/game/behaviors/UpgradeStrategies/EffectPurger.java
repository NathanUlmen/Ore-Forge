package ore.forge.game.behaviors.UpgradeStrategies;

import ore.forge.game.Ore;

@SuppressWarnings("unused")
public class EffectPurger implements UpgradeStrategy {

    public EffectPurger() {

    }

    @Override
    public void applyTo(Ore ore) {
        ore.purgeEffects();
        //might not use this....
        ore.setIsDoomed(false);
    }

    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

}
