package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.components.*;

/**
 * Teleport system teleports the root of an entity to a specific location in world space
 * TeleportRequests applied to non-root entities will have it applied to the root of the entity they are apart of.
 * */
public class TeleportSystem extends IteratingSystem {
    private static final Family FAMILY =
        Family.all(TeleportRequestC.class).get();

    private final Matrix4 tmpWorld = new Matrix4();

    public TeleportSystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final Matrix4 targetWorld = entity.getComponent(TeleportRequestC.class).targetRootWorld;
        final Entity root = getRootEntity(entity);
        final WorldTransformC worldTransformC = root.getComponent(WorldTransformC.class);
        worldTransformC.setBoth(targetWorld);


        //Update physics body
        final PhysicsC p = root.getComponent(PhysicsC.class);
        if (p != null) {
            p.rigidBody.setWorldTransform(targetWorld);

            p.rigidBody.setLinearVelocity(Vector3.Zero);
            p.rigidBody.setAngularVelocity(Vector3.Zero);
            p.rigidBody.activate();
        }
        entity.remove(TeleportRequestC.class);
    }

    private Entity getRootEntity(Entity entity) {
        Entity parent = entity;
        while (true) {
            final ChildC childC = entity.getComponent(ChildC.class);
            if (childC != null && childC.parent != null) {
                parent = childC.parent;
            } else {
                break;
            }
        }
        return parent;
    }


}
