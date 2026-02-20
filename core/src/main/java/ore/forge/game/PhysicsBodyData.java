package ore.forge.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.game.behaviors.BodyLogic;

/**
 * Physics Body data is intended to be used to fill the userData field of {@link btCollisionObject}
 *
 */
public record PhysicsBodyData(Entity entity, Object specificData,
                              BodyLogic bodyLogic, Matrix4 localTransform) {

    @Override
    public String toString() {
        return bodyLogic.toString();
    }
}
