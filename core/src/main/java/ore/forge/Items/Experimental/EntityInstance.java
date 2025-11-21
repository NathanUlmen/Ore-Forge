package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.PhysicsBodyData;
import ore.forge.VisualComponent;

import java.util.List;

public class EntityInstance implements Disposable {
    public List<btCollisionObject> entityPhysicsBodies; //All bodies in this object that relate to physics
    public VisualComponent visualComponent; //Visual components for this item
    private Matrix4 worldTransform;

    public EntityInstance(List<btCollisionObject> collisionObjects, VisualComponent visualComponent) {
        this.entityPhysicsBodies = collisionObjects;
        this.visualComponent = visualComponent;
    }

    //TODO: Add body to render list, register all behaviors,
    public void place(Matrix4 transform) {
        this.setTransform(transform);
        for (var collisionObject : entityPhysicsBodies) {
            assert collisionObject != null;
            if (collisionObject.userData instanceof PhysicsBodyData data) {
                data.bodyLogic.register();
            }
        }
    }

    //Removes body from gameworld, unregisters behaviors, disposes body.
    public void remove() {

    }

    //Should only be used for the initial placement of an item
//    public void setTransform(Matrix4 transform) {
//        for (btCollisionObject object : entityPhysicsBodies) {
//            object.setWorldTransform(transform);
//        }
//        ModelInstance modelInstance = visualComponent.modelInstance;
//        modelInstance.transform.set(transform);
//        modelInstance.calculateTransforms();
//    }

    public void setTransform(Matrix4 transform) {
        for (btCollisionObject object : entityPhysicsBodies) {
            if (object instanceof btRigidBody) {
                object.setWorldTransform(transform);
            }
        }

        visualComponent.modelInstance.transform.set(transform);
        visualComponent.modelInstance.calculateTransforms();

        for (btCollisionObject object : entityPhysicsBodies) {
            if (object instanceof btGhostObject) {
                var userData = (PhysicsBodyData) object.userData;
                if (userData != null) {
                    Matrix4 world = new Matrix4(transform);
                    world.mul(userData.localTransform);
                    object.setWorldTransform(world);
                }
            }
        }
    }

    public Matrix4 transform() {
        return worldTransform;
    }

    @Override
    public void dispose() {
        for (btCollisionObject collisionObject : entityPhysicsBodies) {
            collisionObject.dispose();
        }
    }

}
