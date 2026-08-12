package ore.forge.engine.components.definitions;

import ore.forge.ComponentDefinition;
import ore.forge.engine.components.AnimationC;

public class AnimationDefinition implements ComponentDefinition<AnimationC> {
    @Override
    public AnimationC create() {
        return new AnimationC();
    }
}
