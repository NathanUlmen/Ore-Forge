package ore.forge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;

import java.util.HashSet;
import java.util.Set;

public class CollisionManager extends ContactListener {
    private final Set<CollisionPair> touchingEntities;
    private final GameState gameState;

    //Potential Optimization: Pool CollisionPairs.
    public CollisionManager(GameState gameState) {
        super();
        this.gameState = gameState;
        touchingEntities = new HashSet<>();
    }

    @Override
    public void onContactStarted(btCollisionObject o1, btCollisionObject o2) {
        if (o1.userData instanceof PhysicsBodyData o1Data && o2.userData instanceof PhysicsBodyData o2Data) {
            if (o1Data.bodyLogic != null) {
                o1Data.bodyLogic.onContactStart(o2Data, o1Data, gameState);
            }
            if (o2Data.bodyLogic != null) {
                o2Data.bodyLogic.onContactStart(o1Data, o2Data, gameState);
            }

            var pair = new CollisionPair(o2Data, o1Data);
            touchingEntities.add(pair);
        }

    }

    @Override
    public void onContactEnded(btCollisionObject o1, btCollisionObject o2) {
        if (o1.userData instanceof PhysicsBodyData o1Data && o2.userData instanceof PhysicsBodyData o2Data) {
            if (o1Data.bodyLogic != null) {
                o1Data.bodyLogic.onContactEnd(o2Data, o1Data, gameState);
            }
            if (o2Data.bodyLogic != null) {
                o2Data.bodyLogic.onContactEnd(o1Data, o2Data, gameState);
            }
            var pair = new CollisionPair(o2Data, o1Data);
            touchingEntities.remove(pair);
        }
    }

    public void updateTouchingEntities() {
        final float deltaTime = Gdx.graphics.getDeltaTime();
        for (var pair : touchingEntities) {
            pair.updateTouchingTime(deltaTime);
            if (pair.a() instanceof PhysicsBodyData first && pair.b() instanceof PhysicsBodyData second) {
                if (first.bodyLogic != null) {
                    first.bodyLogic.colliding(second, first, gameState, pair.getTimeTouching());
                }
                if (second.bodyLogic != null) {
                    second.bodyLogic.colliding(first, second, gameState, pair.getTimeTouching());
                }
            }
        }
    }

    public int getNumTouchingEntities() {
        return touchingEntities.size();
    }

    public void removePhysicsBody() {}

}
