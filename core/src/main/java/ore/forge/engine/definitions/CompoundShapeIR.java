package ore.forge.engine.definitions;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;

import java.util.List;

public record CompoundShapeIR(String id, List<Matrix4> transforms, List<PhysicsCollisionShapeIR> collisionShapes) implements PhysicsCollisionShapeIR { }
