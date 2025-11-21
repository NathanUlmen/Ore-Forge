package ore.forge;

import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;

import java.util.HashSet;
import java.util.Set;

public class CollisionManager extends ContactListener {
    private final Set<Pair<PhysicsBodyData>> touchingEntities;

    //Potential Optimization: Pool pairs.
    public CollisionManager() {
        super();
        touchingEntities = new HashSet<>();
    }

    @Override
    public void onContactStarted(btCollisionObject o1, btCollisionObject o2) {
        if (o1.userData instanceof PhysicsBodyData o1Data && o2.userData instanceof PhysicsBodyData o2Data) {
            if (o1Data.bodyLogic != null) {
                o1Data.bodyLogic.onContactStart(o2Data, o1Data);
            }
            if (o2Data.bodyLogic != null) {
                o2Data.bodyLogic.onContactStart(o1Data, o2Data);
            }

            var pair = new Pair<>(o2Data, o1Data);
            touchingEntities.add(pair);
        }

    }

    @Override
    public void onContactEnded(btCollisionObject o1, btCollisionObject o2) {
        if (o1.userData instanceof PhysicsBodyData o1Data && o2.userData instanceof PhysicsBodyData o2Data) {
            if (o1Data.bodyLogic != null) {
                o1Data.bodyLogic.onContactEnd(o2Data, o1Data);
            }
            if (o2Data.bodyLogic != null) {
                o2Data.bodyLogic.onContactEnd(o1Data, o2Data);
            }
            var pair = new Pair<>(o2Data, o1Data);
            touchingEntities.remove(pair);
        }
    }

    public void updateTouchingEntities() {
        for (var pair : touchingEntities) {
            if (pair.first() instanceof PhysicsBodyData first && pair.second() instanceof PhysicsBodyData second) {
                if (first.bodyLogic != null) {
                    first.bodyLogic.colliding(second, first);
                }
                if (second.bodyLogic != null) {
                    second.bodyLogic.colliding(first, second);
                }
            }
        }
    }

    public int getNumTouchingEntities() {
        return touchingEntities.size();
    }


}
