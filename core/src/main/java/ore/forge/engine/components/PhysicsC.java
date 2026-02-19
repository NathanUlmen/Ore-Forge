package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class PhysicsC implements Component {
    public enum BodyType {DYNAMIC, STATIC, KINEMATIC}

    public btRigidBody rigidBody; //handle to bullet object
    public btCollisionShape collisionShape; //optional
    public BodyType type;
}
