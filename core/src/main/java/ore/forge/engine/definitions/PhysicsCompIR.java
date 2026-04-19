package ore.forge.engine.definitions;

import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import ore.forge.engine.PhysicsBodyType;
import ore.forge.engine.PhysicsMotionType;

/**
 * @author Nathan Ulmen
 * {@link PhysicsCompIR} - Physics Component Intermidiate Representation
 * Conatins data needed to create a {@link btCollisionShape}
 * Collision Group - ENUM - Denotes group the element is apart of
 * Collision Mask - ENUM - Denotes group the element can collide with
 * <p>
 * linear damping
 * angular damping
 * Transform offsets?
 */
public record PhysicsCompIR(String id,
                            PhysicsBodyType bodyType,
                            PhysicsMotionType motionType,
                            float mass,
                            float friction,
                            float restitution,
                            PhysicsCollisionShapeIR collisionShape) {
}
