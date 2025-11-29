package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.PhysicsBodyData;
import ore.forge.PhysicsWorld;
import ore.forge.VisualComponent;

import java.util.List;

public class EntityInstance implements Disposable {
    public List<btCollisionObject> entityPhysicsBodies;
    public VisualComponent visualComponent;

    // Single authoritative transform
    private final Matrix4 worldTransform = new Matrix4();

    public EntityInstance(List<btCollisionObject> collisionObjects, VisualComponent visualComponent) {
        this.entityPhysicsBodies = collisionObjects;
        this.visualComponent = visualComponent;
    }

    public void place(Matrix4 transform) {
        setTransform(transform);

        for (btCollisionObject body : entityPhysicsBodies) {
            if (body.userData instanceof PhysicsBodyData data && data.bodyLogic != null) {
                data.bodyLogic.register();
            }
        }
    }

    public void remove() {
        //TODO: update collision Manager too.
        visualComponent.dispose();
        var world = PhysicsWorld.instance().dynamicsWorld();
        for (btCollisionObject body : entityPhysicsBodies) {
            if (body.userData instanceof PhysicsBodyData data && data.bodyLogic != null) {
                data.bodyLogic.unregister();
            }
            world.removeCollisionObject(body);
            body.dispose();
        }
    }

    /**
     * Sets the ENTIRE entity's transform.
     * Keeps physics bodies + visuals perfectly in sync.
     */
    private void setTransform(Matrix4 transform) {
        // Copy into authoritative transform
        worldTransform.set(transform);

        // 1. Update Rigid Bodies directly
        for (btCollisionObject body : entityPhysicsBodies) {
            if (body instanceof btRigidBody) {
                body.setWorldTransform(worldTransform);
            }
        }

        // 2. Update Visual
        visualComponent.modelInstance.transform.set(worldTransform);
        visualComponent.modelInstance.calculateTransforms();

        // 3. Update Ghost Objects relative to their local offsets
        for (btCollisionObject body : entityPhysicsBodies) {
            if (body instanceof btGhostObject && body.userData instanceof PhysicsBodyData data) {
                Matrix4 ghostWorld = new Matrix4(worldTransform).mul(data.localTransform);
                body.setWorldTransform(ghostWorld);
            }
            if (body instanceof btRigidBody rigid) {
                rigid.setWorldTransform(worldTransform);
                rigid.getMotionState().setWorldTransform(worldTransform);
            }
        }
    }



    public Matrix4 transform() {
        return worldTransform;
    }

    @Override
    public void dispose() {
        for (btCollisionObject body : entityPhysicsBodies) {
            body.dispose();
        }
    }
}
