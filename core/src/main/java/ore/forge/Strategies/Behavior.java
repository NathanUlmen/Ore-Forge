package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import ore.forge.Items.Experimental.ItemUserData;

public interface Behavior {
    void register();

    void unregister();

    void attach(Body body, Fixture fixture);

    void update(float delta);

    void interact(Object subjectData, ItemUserData userData);

    Behavior clone(Fixture parent);

    boolean isCollisionBehavior();

}
