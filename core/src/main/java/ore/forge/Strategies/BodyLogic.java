package ore.forge.Strategies;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.ItemUserData;
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
     * Attaches the behavior to the specified objects
     *
     */
    void attach(ItemSpawner spawner, btCollisionObject collisionObject);

    /**
     * Called when updating time sensitive information
     *
     */
    void update(float delta); //Optional


    /**
     * Called when the two bodies begin contact
     */
    @Deprecated
    void onContactStart(Object subjectData, ItemUserData userData);
    void onContactStart(PhysicsBodyData subject, PhysicsBodyData source);

    /**
     * Called when resolving collision based interactions
     *
     */
    @Deprecated
    void colliding(Object subjectData, ItemUserData userData);
    void colliding(PhysicsBodyData subject, PhysicsBodyData source, float timeTouching);

    /**
     * Called when the two dies end contact
     *
     */
    @Deprecated
    void onContactEnd(Object subjectData, ItemUserData userData);
    void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source);

    /**
     * Used to return a new instance of this behavior, replicating things that aren't state
     */
    BodyLogic clone();

}
