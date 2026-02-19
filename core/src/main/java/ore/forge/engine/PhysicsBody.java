package ore.forge.engine;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.game.PhysicsBodyData;
import ore.forge.game.GameContext;


public class PhysicsBody implements Component {
    public enum PhysicsBodyType {DYNAMIC, STATIC, KINEMATIC}

    public btCollisionObject bodyHandle; //always set
    public btRigidBody rigidBody; //set for dynamic/kinematic else null
    public PhysicsBodyType bodyType; //

}
