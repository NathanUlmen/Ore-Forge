package ore.forge.engine.definitions;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import ore.forge.ComponentDefinition;
import ore.forge.engine.Pair;
import ore.forge.engine.PhysicsBodyType;
import ore.forge.engine.PhysicsMotionType;
import ore.forge.engine.components.PhysicsC;

/**
 * @author Nathan Ulmen
 * {@link PhysicsDefinition} - Physics Component Intermidiate Representation
 * Conatins data needed to create a {@link btCollisionShape}
 * Collision Group - ENUM - Denotes group the element is apart of
 * Collision Mask - ENUM - Denotes group the element can collide with
 * <p>
 * linear damping
 * angular damping
 * Transform offsets?
 */
public record PhysicsDefinition(String id,
                                PhysicsBodyType bodyType,
                                PhysicsMotionType motionType,
                                float mass,
                                float friction,
                                float restitution,
                                PhysicsCollisionShapeIR collisionShape) implements ComponentDefinition<PhysicsC> {
    @Override
    public PhysicsC create() {
        PhysicsC component = new PhysicsC();
        component.bodyType = this.bodyType;
        component.motionType = this.motionType;

        component.collisionObject = switch (component.bodyType) {
            case RIGID -> new btRigidBody(mass, new btDefaultMotionState(), createCollisionShape(collisionShape));
            case GHOST -> new btGhostObject();
        };

        component.collisionObject.setFriction(friction);
        component.collisionObject.setRestitution(restitution);
        return component;
    }

    public static btCollisionShape createCollisionShape(PhysicsCollisionShapeIR collisionShape) {
        return switch (collisionShape) {
            case BoxShapeIR box -> new btBoxShape(box.halfExtents().min);
            case PlaneShapeIR plane -> new btStaticPlaneShape(plane.planeNormal(), plane.planeConstant());
            case SphereShapeIR sphere -> new btSphereShape(sphere.radius());
            case CapsuleShapeIR capsule -> new btCapsuleShape(capsule.radius(), capsule.height());
            case CompoundShapeIR compound -> {
                btCompoundShape compoundShape = new btCompoundShape();
                for (Pair<Matrix4, PhysicsCollisionShapeIR> shape : compound.collisionShapes()) {
                    compoundShape.addChildShape(shape.first, createCollisionShape(shape.second));
                }
                yield compoundShape;
            }
        };
    }

}
