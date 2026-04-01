package ore.forge.engine.definitions;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import ore.forge.engine.components.PhysicsBodyType;
import ore.forge.engine.components.PhysicsMotionType;

/**
 * @author Nathan Ulmen
 * {@link PhysicsCompIR} - Physics Component Intermidiate Representation
 * Conatins data needed to create a {@link btCollisionShape}
 */
public record PhysicsCompIR(PhysicsBodyType bodyType, PhysicsMotionType motionType,
                            PhysicsCollisionShapeIR collisionShape) {
}
