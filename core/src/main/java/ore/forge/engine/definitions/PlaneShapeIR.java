package ore.forge.engine.definitions;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;

/**
 * @author Nathan Ulmen
 * Intermidiary representation for a {@link btStaticPlaneShape}
 *
 * */
public record PlaneShapeIR(Vector3 planeNormal, float planeConstant) implements PhysicsCollisionShapeIR {
}
