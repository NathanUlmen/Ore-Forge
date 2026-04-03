package ore.forge.engine.definitions;

import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

/**
 * @author Nathan Ulmen
 * ShpereShapeIR conatains all data needed to create a {@link btSphereShape}.
 * */
public record SphereShapeIR(String id, float radius) implements PhysicsCollisionShapeIR { }
