package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btGhostObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class PhysicsC implements Component {

    public enum MotionType {DYNAMIC, STATIC, KINEMATIC}
    public enum BodyType {RIGID, GHOST}


    public btCollisionObject collisionObject; //handle to bullet object
    public MotionType motionType;
    public BodyType bodyType;

    public record Recipe(MotionType motionType, btCollisionShape localFromEntity) {

    }

    public btRigidBody asRigidBody() {
        return bodyType == BodyType.RIGID ? (btRigidBody) collisionObject : null;
    }

    public btGhostObject asGhost() {
        return bodyType == BodyType.GHOST ? (btGhostObject) collisionObject : null;
    }

}
