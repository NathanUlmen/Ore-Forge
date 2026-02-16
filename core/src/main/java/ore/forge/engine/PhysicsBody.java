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
    private static final Matrix4 tmp = new Matrix4();
    private btCollisionObject body;

    // current + previous pose (authoritative)
    private final Vector3 prevPos = new Vector3();
    private final Vector3 currPos = new Vector3();
    private final Quaternion prevRot = new Quaternion();
    private final Quaternion currRot = new Quaternion();
    private final Vector3 prevScale = new Vector3();
    private final Vector3 currScale = new Vector3();

    // temp for building transforms
    private final Vector3 renderPos = new Vector3();
    private final Quaternion renderRot = new Quaternion();
    private final Vector3 renderScale = new Vector3();
    private final Matrix4 renderTransform = new Matrix4();


    private Matrix4 localTransform; //entity local offset

    public PhysicsBody(btCollisionObject body, Matrix4 localTransform, int groupMask, int collideMask) {
        this.body = body;
        this.localTransform = new Matrix4();
        this.localTransform.set(localTransform);
    }

    public void syncFromEntity(Matrix4 entityWorld) {
        tmp.set(entityWorld).mul(localTransform);
        teleport(tmp);
    }

    public void snapshotPrev() {
        // prev = curr (do this once per fixed step, before stepping)
        prevPos.set(currPos);
        prevRot.set(currRot);
        prevScale.set(currScale);
    }

    public void readFromBullet() {
        // update curr from bullet after stepping
        body.getWorldTransform(tmp);
        tmp.getTranslation(currPos);
        tmp.getRotation(currRot);
        tmp.getScale(currScale);
    }

    public Matrix4 getRenderTransform(float alpha) {
        renderPos.set(prevPos).lerp(currPos, alpha);
        renderRot.set(prevRot).slerp(currRot, alpha);
        renderScale.set(prevScale).slerp(currScale, alpha);

        renderTransform.idt();
        renderTransform.translate(renderPos);
        renderTransform.rotate(renderRot);
        renderTransform.scl(renderScale);
        return renderTransform;
    }

    public void teleport(Matrix4 newWorld) {
        body.setWorldTransform(newWorld);
        newWorld.getTranslation(currPos);
        newWorld.getRotation(currRot);
        newWorld.getScale(currScale);
        prevPos.set(currPos);
        prevRot.set(currRot);
        prevScale.set(currScale);
    }

    public void syncToEntity(Matrix4 outEntityTransform) {
        if (body instanceof btRigidBody rb) {
            rb.getMotionState().getWorldTransform(outEntityTransform);
        }
    }

    public void add(btDynamicsWorld world) {
        if (body instanceof btRigidBody rb) {
            world.addRigidBody(rb);
        } else {
            world.addCollisionObject(body);
        }

        if (body.userData instanceof PhysicsBodyData data) {
            if (data.bodyLogic != null) {
                data.bodyLogic.register(GameContext.INSTANCE);
            }
        }
    }

    public void remove(btDynamicsWorld world) {
        if (body instanceof btRigidBody rb) {
            world.removeRigidBody(rb);
        } else {
            world.removeCollisionObject(body);
        }
    }

    public btCollisionObject getRigidBody() {
        return body;
    }

    @Override
    public void dispose() {
        if (body instanceof btRigidBody rb) {
            rb.getMotionState().dispose();
        }
        body.dispose();
    }
}
