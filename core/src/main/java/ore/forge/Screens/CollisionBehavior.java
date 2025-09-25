package ore.forge.Screens;

import com.badlogic.gdx.physics.box2d.Fixture;
import ore.forge.Items.Experimental.ItemBlueprint;

public interface CollisionBehavior {

    void interact(Fixture contact, ItemBlueprint.ItemUserData userData);

    CollisionBehavior clone(Fixture parent);


}
