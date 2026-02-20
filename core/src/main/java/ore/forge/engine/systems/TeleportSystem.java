package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.components.*;

public class TeleportSystem extends IteratingSystem {
    private static final Family FAMILY =
        Family.all(TeleportRequestC.class).one(TransformC.class, PhysicsC.class,
            ParentC.class
        ).get();

    private final Matrix4 tmpWorld = new Matrix4();

    public TeleportSystem() {
        super(FAMILY);
    }

    /**
     * When we teleport an entity we must do the following:
     * Set its transform component to the target
     * If it has a history set prev and current to target
     * Set the PhysicsC bodyHandle transform to target
     * Adjust RenderComponent to be correct if present.
     * If it's a parent component we must do that for all its children
     *
     */
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final Matrix4 targetWorld = entity.getComponent(TeleportRequestC.class).targetRootWorld;
        entity.remove(TeleportRequestC.class);

        //Update TransformC (LOCAL canonical)
        final TransformC t = entity.getComponent(TransformC.class);
        if (t != null) {
            final ChildC child = entity.getComponent(ChildC.class);

            // If we inherit from a parent, convert target WORLD -> LOCAL
            if (child != null && child.inheritTransform && child.parent != null) {
                // You need parentWorld matrix from your world-cache or transform system.
                // Assume you have it in a WorldTransformC:
                final WorldTransformC parentW = child.parent.getComponent(WorldTransformC.class);

                if (parentW != null) {
                    // local = inverse(parentWorld) * targetWorld
                    tmpWorld.set(parentW.currentTransform).inv().mul(targetWorld);
                    t.setBothLocal(tmpWorld);
                } else {
                    t.setBothLocal(targetWorld);
                }
            } else {
                // Root: local == world
                t.setBothLocal(targetWorld);
            }
        }

        //Update physics body
        final PhysicsC p = entity.getComponent(PhysicsC.class);
        if (p != null) {
            p.rigidBody.setWorldTransform(targetWorld);

            p.rigidBody.setLinearVelocity(Vector3.Zero);
            p.rigidBody.setAngularVelocity(Vector3.Zero);
            p.rigidBody.activate();
        }
        entity.remove(TeleportRequestC.class);
    }


}
