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
            var pair = new  Pair<>(o1.userData, o2.userData);
            if (pair.second() instanceof ItemUserData itemData) {
                itemData.behavior().onContactStart(pair.first(), itemData);
            } else if (pair.first() instanceof ItemUserData itemData) {
                itemData.behavior().onContactStart(pair.second(), itemData);
            }
            touchingEntities.add(pair);
        }
    }

    @Override
    public void onContactEnded(btCollisionObject o1, btCollisionObject o2) {
        var pair = new Pair<>(o1.userData, o2.userData);
        if (pair.second() instanceof ItemUserData itemData) {
            itemData.behavior().onContactEnd(pair.first(), itemData);
        } else if (pair.first() instanceof ItemUserData itemData) {
            itemData.behavior().onContactEnd(pair.second(), itemData);
        }
        touchingEntities.remove(pair);
    }

    public int getNumTouchingEntities() {
        return touchingEntities.size();
    }

    public void updateTouchingEntities() {
        for (var pair : touchingEntities) {
            if (pair.second() instanceof ItemUserData itemData) {
                itemData.behavior().colliding(pair.first(), itemData);
            } else if (pair.first() instanceof ItemUserData itemData) {
                itemData.behavior().colliding(pair.second(), itemData);
            }
        }
    }

}
