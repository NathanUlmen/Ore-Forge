package ore.forge.game.behaviors.OreEffects;

import ore.forge.game.Ore;

@SuppressWarnings("unused")
public interface OreEffect {
    void activate(float deltaT, Ore ore);

    //Whenever an Ore Effect is applied to an ore the clone method is called.
    OreEffect cloneOreEffect();

}
