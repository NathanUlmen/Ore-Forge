package ore.forge.engine.definitions;

import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;

/**
 * @author Nathan Ulmen
 * CapsuleShapeIR contains all data needed to create a {@link btCapsuleShape}
 *
 */
public record CapsuleShapeIR(float radius, float height) implements PhysicsCollisionShapeIR { }
