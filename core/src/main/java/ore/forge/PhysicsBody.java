package ore.forge;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;


public class PhysicsBody implements Disposable {
    private static final Matrix4 tmp = new Matrix4();
    private btCollisionObject body;
    private Matrix4 localTransform;

    public PhysicsBody(btCollisionObject body, Matrix4 localTransform, int groupMask, int collideMask) {
        this.body = body;
        this.localTransform = new Matrix4();
        this.localTransform.set(localTransform);
    }

    public void syncFromEntity(Matrix4 entityTransform) {
        tmp.set(entityTransform).mul(localTransform);

        if (body instanceof btRigidBody rb) {
            rb.setWorldTransform(tmp);
            rb.getMotionState().setWorldTransform(tmp);
            rb.activate();
        } else {
            body.setWorldTransform(tmp);
        }
    }

    public void syncToEntity(Matrix4 outEntityTransform) {
        if (body instanceof btRigidBody rb) {
            rb.getMotionState().getWorldTransform(outEntityTransform);
        }
    }

    public void add(btDynamicsWorld world) {
        System.out.println(localTransform);
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
