package ore.forge.engine;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.game.PhysicsBodyData;
import ore.forge.game.GameContext;


public class PhysicsBody implements Disposable {
    public enum PhysicsBodyType {DYNAMIC, STATIC, KINEMATIC}

    public btCollisionObject bodyHandle; //handle to bullet PhysicsObject
    public PhysicsBodyType bodyType; //
    public Matrix4 localFromRoot; //

    public PhysicsBody(PhysicsBodyType bodyType, Matrix4 localFromRoot) {
        this.bodyType = bodyType;
        this.localFromRoot = new Matrix4(localFromRoot);
    }


//    public PhysicsBody(btCollisionObject body, Matrix4 localTransform, int groupMask, int collideMask) {
//        this.bodyHandle = body;
//        this.localTransform = new Matrix4();
//        this.localTransform.set(localTransform);
//    }
//
//    public void syncFromEntity(Matrix4 entityWorld) {
//        tmp.set(entityWorld).mul(localTransform);
//        teleport(tmp);
//    }
//
//    public void snapshotPrev() {
//        // prev = curr (do this once per fixed step, before stepping)
//        prevPos.set(currPos);
//        prevRot.set(currRot);
//        prevScale.set(currScale);
//    }
//
//    public void readFromBullet() {
//        // update curr from bullet after stepping
//        bodyHandle.getWorldTransform(tmp);
//        tmp.getTranslation(currPos);
//        tmp.getRotation(currRot);
//        tmp.getScale(currScale);
//    }
//
//    public Matrix4 getRenderTransform(float alpha) {
//        renderPos.set(prevPos).lerp(currPos, alpha);
//        renderRot.set(prevRot).slerp(currRot, alpha);
//        renderScale.set(prevScale).slerp(currScale, alpha);
//
//        renderTransform.idt();
//        renderTransform.translate(renderPos);
//        renderTransform.rotate(renderRot);
//        renderTransform.scl(renderScale);
//        return renderTransform;
//    }
//
//    public void teleport(Matrix4 newWorld) {
//        bodyHandle.setWorldTransform(newWorld);
//        newWorld.getTranslation(currPos);
//        newWorld.getRotation(currRot);
//        newWorld.getScale(currScale);
//        prevPos.set(currPos);
//        prevRot.set(currRot);
//        prevScale.set(currScale);
//    }
//
//    public void syncToEntity(Matrix4 outEntityTransform) {
//        if (bodyHandle instanceof btRigidBody rb) {
//            rb.getMotionState().getWorldTransform(outEntityTransform);
//        }
//    }
//
//    public void add(btDynamicsWorld world) {
//        if (bodyHandle instanceof btRigidBody rb) {
//            world.addRigidBody(rb);
//        } else {
//            world.addCollisionObject(bodyHandle);
//        }
//
//        if (bodyHandle.userData instanceof PhysicsBodyData data) {
//            if (data.bodyLogic != null) {
//                data.bodyLogic.register(GameContext.INSTANCE);
//            }
//        }
//    }
//
//    public void remove(btDynamicsWorld world) {
//        if (bodyHandle instanceof btRigidBody rb) {
//            world.removeRigidBody(rb);
//        } else {
//            world.removeCollisionObject(bodyHandle);
//        }
//    }
//
//    public btCollisionObject getRigidBody() {
//        return bodyHandle;
//    }
//
    @Override
    public void dispose() {
        if (bodyHandle instanceof btRigidBody rb) {
            rb.getMotionState().dispose();
        }
        bodyHandle.dispose();
    }

}
