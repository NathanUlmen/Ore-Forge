package ore.forge.engine.definitions;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.utils.Array;
import ore.forge.engine.Pair;

import java.util.List;

public record CompoundShapeIR(Array<Pair<Matrix4, PhysicsCollisionShapeIR>> collisionShapes) implements PhysicsCollisionShapeIR {
}
