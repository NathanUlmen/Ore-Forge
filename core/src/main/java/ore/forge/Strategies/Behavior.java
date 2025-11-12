package ore.forge.Strategies;

import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderSpawner;

public interface Behavior {
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
    void attach(UpgraderSpawner spawner, btCollisionObject collisionObject);

    /**
     * Called when updating time sensitive information
     *
     */
    void update(float delta);


    /**
     * Called when the two bodies begin contact
     */
    void onContactStart(Object subjectData, ItemUserData userData);

    /**
     * Called when resolving collision based interactions
     *
     */
    void colliding(Object subjectData, ItemUserData userData);

    /**
     * Called when the two dies end contact
     *
     */
    void onContactEnd(Object subjectData, ItemUserData userData);

    /**
     * Used to return a new instance of this behavior, replicating things that aren't state
     */
    Behavior clone();

    boolean isCollisionBehavior();

}
