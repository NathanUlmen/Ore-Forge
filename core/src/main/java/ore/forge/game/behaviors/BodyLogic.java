package ore.forge.game.behaviors;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.game.GameContext;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.PhysicsBodyData;

public interface BodyLogic {
    /**
     * Used to register to specific event systems
     *
     */
    void register(GameContext context);

    /**
     * Used to unregister from event and or update systems
     */
    void unregister(GameContext context);

    /**
     * Called when updating time sensitive information
     *
     */
    default void update(float delta) {
    }

    /**
     * Attaches the behavior to the specified objects
     *
     */
    void attach(ItemDefinition definition, btCollisionObject collisionObject);

    void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context);

    void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching);

    void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context);

    /**
     * Used to return a new instance of this behavior, replicating things that aren't state
     */
    BodyLogic clone();

}
