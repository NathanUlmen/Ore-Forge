package ore.forge.engine.definitions;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;

/**
 * @author Nathan Ulmen
 * BoxShapeIR contains all data needed to create a {@link btBoxShape}
 *
 */
public record BoxShapeIR(String id, BoundingBox halfExtents) implements PhysicsCollisionShapeIR {

}
