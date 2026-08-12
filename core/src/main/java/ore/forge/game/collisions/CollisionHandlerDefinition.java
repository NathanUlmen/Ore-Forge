package ore.forge.game.collisions;

import ore.forge.ComponentDefinition;

public class CollisionHandlerDefinition implements ComponentDefinition<CollisionHandlerC> {
    @Override
    public CollisionHandlerC create() {
        return new CollisionHandlerC();
    }
}
