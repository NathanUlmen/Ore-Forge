package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class PhysicsC implements Component {

    public btCollisionObject collisionObject; //handle to bullet object
    public PhysicsMotionType motionType;
    public PhysicsBodyType bodyType;

    public btRigidBody asRigidBody() {
        return bodyType == PhysicsBodyType.RIGID ? (btRigidBody) collisionObject : null;
    }

    public btGhostObject asGhost() {
        return bodyType == PhysicsBodyType.GHOST ? (btGhostObject) collisionObject : null;
    }

}
