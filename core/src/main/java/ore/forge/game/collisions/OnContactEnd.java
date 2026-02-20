package ore.forge.game.collisions;

import com.badlogic.ashley.core.Entity;
import ore.forge.game.GameContext;
import ore.forge.game.GameContext2;

@FunctionalInterface
public interface OnContactEnd {
    void onEnd(Entity self, Entity other, CollisionEvent e, GameContext2 context);
}
