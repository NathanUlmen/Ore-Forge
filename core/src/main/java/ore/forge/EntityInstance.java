package ore.forge;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Strategies.Updatable;

import java.util.ArrayList;
import java.util.List;

public class EntityInstance implements Disposable {
    public final List<Updatable> updatables;
    public final List<GameEventListener<?>> listeners;
    public final PhysicsComponent physicsComponent;
    public VisualComponent visualComponent;
    private final Matrix4 worldTransform;

    public EntityInstance(PhysicsComponent physicsComponent, VisualComponent visualComponent) {
        worldTransform = new Matrix4();
        this.physicsComponent = physicsComponent;
        this.updatables = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.visualComponent = visualComponent;
        visualComponent.modelInstance.transform = worldTransform;
    }


    public void addToWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.addToWorld(dynamicsWorld);
        syncFromPhysics();
//        for (btCollisionObject collisionObject : entityPhysicsBodies) {
//            collisionObject.setWorldTransform(worldTransform);
//            dynamicsWorld.addCollisionObject(collisionObject);
//        }
    }

    public void removeFromWorld(btDynamicsWorld dynamicsWorld) {
        physicsComponent.removeFromWorld(dynamicsWorld);
    }

    public void setTransform(Matrix4 worldTransform) {
        this.worldTransform.set(worldTransform);
        physicsComponent.setWorldTransform(worldTransform);
        visualComponent.modelInstance.transform.set(worldTransform);
    }

    public void syncFromPhysics() {
        physicsComponent.syncToEntity(worldTransform);
        visualComponent.syncFromEntity(worldTransform);
    }

//    public void place(Matrix4 transform) {
//        setTransform(transform);
//        for (btCollisionObject body : entityPhysicsBodies) {
//            if (body.userData instanceof PhysicsBodyData data && data.bodyLogic != null) {
//                data.bodyLogic.register();
//            }
//        }
//    }
//
//    public void setTransform(Matrix4 transform) {
//        worldTransform.set(transform);
//
//        for (btCollisionObject body : entityPhysicsBodies) {
//            if (body instanceof btRigidBody) {
//                body.setWorldTransform(worldTransform);
//            }
//        }
//
//        visualComponent.modelInstance.transform.set(worldTransform);
//        visualComponent.modelInstance.calculateTransforms();
//
//        for (btCollisionObject body : entityPhysicsBodies) {
//            if (body instanceof btGhostObject && body.userData instanceof PhysicsBodyData data) {
//                Matrix4 ghostWorld = new Matrix4(worldTransform).mul(data.localTransform);
//                body.setWorldTransform(ghostWorld);
//            }
//            if (body instanceof btRigidBody rigid) {
//                rigid.setWorldTransform(worldTransform);
//                rigid.getMotionState().setWorldTransform(worldTransform);
//            }
//        }
//    }

    public Matrix4 transform() {
        return worldTransform;
    }

    @Override
    public void dispose() {
        physicsComponent.dispose();
        visualComponent.dispose();
    }
}
