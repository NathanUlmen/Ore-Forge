package ore.forge.game.collisions;

import com.badlogic.ashley.core.Entity;
import ore.forge.game.GameContext;

@FunctionalInterface
public interface OnContactStart {
    void onStart(Entity self, Entity other, CollisionEvent e, GameContext context);
}
