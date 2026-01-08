package ore.forge.Strategies;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.Items.ItemDefinition;
import ore.forge.PhysicsBodyData;

public interface BodyLogic {
    /**
     * Used to register to specific event systems
     *
     */
    void register();

    /**
     * Used to unregister from event and or update systems
     */
    void unregister();

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

    void onContactStart(PhysicsBodyData subject, PhysicsBodyData source);

    void colliding(PhysicsBodyData subject, PhysicsBodyData source, float timeTouching);

    void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source);

    /**
     * Used to return a new instance of this behavior, replicating things that aren't state
     */
    BodyLogic clone();

}
