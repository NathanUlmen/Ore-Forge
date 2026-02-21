package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class PhysicsC implements Component {
    public void btR() {

    }

    public enum BodyType {DYNAMIC, STATIC, KINEMATIC}

    public btRigidBody rigidBody; //handle to bullet object
    public btCollisionShape collisionShape; //optional
    public BodyType type;

    public record Recipe(BodyType bodyType, btCollisionShape localFromEntity) {

    }

}
