package ore.forge.game;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.engine.EntityInstance;
import ore.forge.game.behaviors.BodyLogic;

/**
 * Physics Body data is intended to be used to fill the userData field of {@link btCollisionObject}
 *
 */
public class PhysicsBodyData {
    public final EntityInstance parentEntityInstance;
    public final Object specificData;
    public final BodyLogic bodyLogic;
    public final Matrix4 localTransform;

    public PhysicsBodyData(EntityInstance parentEntityInstance, Object specificData, BodyLogic bodyLogic,  Matrix4 localTransform) {
        this.parentEntityInstance = parentEntityInstance;
        this.specificData = specificData;
        this.bodyLogic = bodyLogic;
        this.localTransform =  localTransform;
    }

    @Override
    public String toString() {
        return bodyLogic.toString();
    }
}
