package ore.forge.engine.Components;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.engine.PhysicsBody;

import java.util.List;

public class PhysicsComponent implements Disposable {
    private List<PhysicsBody> bodies;
    private Matrix4 worldTransform;

    public PhysicsComponent(List<PhysicsBody> bodies) {
        this.bodies = bodies;
        worldTransform = new Matrix4();
    }

    public List<PhysicsBody> getBodies() {
        return bodies;
    }

    public void setWorldTransform(final Matrix4 worldTransform) {
        this.worldTransform.set(worldTransform);
        for (PhysicsBody body : bodies) {
            body.syncFromEntity(this.worldTransform);
        }
    }

    public void syncToEntity(Matrix4 outEntityTransform) {
        if (bodies.isEmpty()) return;
        PhysicsBody body = bodies.getFirst();
        body.syncToEntity(outEntityTransform);
        worldTransform.set(outEntityTransform);
    }

    public void addToWorld(btDynamicsWorld world) {
        for (PhysicsBody body : bodies) {
            body.syncFromEntity(worldTransform);
            body.add(world);
        }
    }

    public void removeFromWorld(btDynamicsWorld world) {
        for (PhysicsBody body : bodies) {
            body.remove(world);
        }
    }

    public void dispose() {
        for (PhysicsBody part : bodies) {
            part.dispose();
        }
    }

    public btCollisionObject getRigidBody() {
        return bodies.getFirst().getRigidBody();
    }
}
