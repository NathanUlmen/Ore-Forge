package ore.forge.game.behaviors.UpgradeStrategies;

import ore.forge.ButtonHelper;
import ore.forge.game.expressions.Operands.NumericOreProperties;
import ore.forge.game.expressions.Operators.NumericOperator;
import ore.forge.game.Ore;
import ore.forge.OreRealm;
import ore.forge.game.behaviors.OreEffects.OreEffect;
import ore.forge.game.behaviors.OreEffects.UpgradeOreEffect;

import java.util.Random;

@SuppressWarnings("unused")
public class ReplicateStrategy implements OreEffect, UpgradeStrategy {
    private final static OreRealm oreRealm = OreRealm.getSingleton();
    private final Random rand;

    public ReplicateStrategy() {
        rand = new Random();
    }

    @Override
    public void activate(float deltaT, Ore ore) {
        applyTo(ore);
    }

    @Override
    public void applyTo(Ore ore) {
        if ((rand.nextFloat() * 100) < 50 && !oreRealm.getStackOfOre().isEmpty()) {
            ButtonHelper.playFurnaceSellSound();
            Ore replicant = oreRealm.queueOre();
//            replicant.setVector(ore.getVector());
//            replicant.setDestination(ore.getDestination(), ore.getMoveSpeed(), ore.getDirection());
//            var bundle = new BundledUpgrade(new BasicUpgrade(0.1, NumericOperator.ASSIGNMENT, NumericOreProperties.SPEED_SCALAR));
//            replicant.applyEffect(new UpgradeOreEffect(.1f, .1f, bundle)); //Slows ore down for a time
//            replicant.applyEffect(new UpgradeOreEffect(.2f, .2f, new BasicUpgrade(1, NumericOperator.ASSIGNMENT, NumericOreProperties.SPEED_SCALAR))); //Returns to normal speed
            replicant.applyEffect(new UpgradeOreEffect(3f, 3f, this));
            replicant.applyBaseStats(ore.getOreValue(), ore.getOreTemp(), ore.getMultiOre(), ore.getName(), ore.getID(), null);
        }
    }


    @Override
    public UpgradeStrategy cloneUpgradeStrategy() {
        return this;
    }

    @Override
    public OreEffect cloneOreEffect() {
        return this;
    }


}
