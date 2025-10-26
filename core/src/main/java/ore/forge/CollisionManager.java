package ore.forge;

import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.Items.Experimental.ItemUserData;

import java.util.HashSet;
import java.util.Set;

public class CollisionManager extends ContactListener {
    private final Set<Pair<?>> touchingEntities;

    public CollisionManager() {
        super();
        touchingEntities = new HashSet<>();
    }

    @Override
    public void onContactStarted(btCollisionObject o1, btCollisionObject o2) {
        if (!(o1.userData instanceof ItemUserData && o2.userData instanceof ItemUserData)) {
            touchingEntities.add(new Pair<>(o1.userData, o2.userData));
        }
    }

    @Override
    public void onContactEnded(btCollisionObject o1, btCollisionObject o2) {
        touchingEntities.remove(new Pair<>(o1.userData, o2.userData));
    }

    public int getNumTouchingEntities() {
        return touchingEntities.size();
    }

    public void updateTouchingEntities() {
        for (var pair : touchingEntities) {
            if (pair.second() instanceof ItemUserData itemData) {
                itemData.behavior().interact(pair.first(), itemData);
            } else if (pair.first() instanceof ItemUserData itemData) {
                itemData.behavior().interact(pair.second(), itemData);
            }
        }
    }

}
