package ore.forge.game.behaviors.OreEffects;

import com.badlogic.gdx.utils.JsonValue;
import ore.forge.game.Ore;
import ore.forge.engine.ReflectionLoader;

@SuppressWarnings("unused")
public class BundledOreEffect implements OreEffect {
    private final OreEffect[] strategies;

    public BundledOreEffect(OreEffect... effects) {
        strategies = new OreEffect[effects.length];
        System.arraycopy(effects, 0, strategies, 0, strategies.length);
    }

    public BundledOreEffect(JsonValue jsonValue) {
//        strategies = new OreEffect[jsonValue.size];
//        for (int i = 0; i < jsonValue.size; i++) {
//            strategies[i] = createOrNull(jsonValue, "effect" + String.valueOf(i+1), "effectName");
//        }

        var effectArray = jsonValue.get("effects");
        strategies = new OreEffect[effectArray.size];
        for (int i = 0; i < effectArray.size; i++) {
            strategies[i] = ReflectionLoader.load(effectArray.get(i), "effectName");
        }
    }

    @Override
    public void activate(float deltaT, Ore ore) {
        throw new RuntimeException("WHY IS BUNDLED EFFECT being activated");
    }


    @Override
    public OreEffect cloneOreEffect() {
        return this;
    }

    public OreEffect[] getStrategies() {
        return strategies;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (OreEffect strategy : strategies) {
            if (strategy != null) {
                s.append("\n").append(strategy);
            }
        }
        return String.valueOf(s);
    }

}
