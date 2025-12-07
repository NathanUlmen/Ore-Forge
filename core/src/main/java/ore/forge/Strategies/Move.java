package ore.forge.Strategies;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Ore;
import ore.forge.PhysicsBodyData;

public class Move implements BodyLogic {
    private final float force;
    private btCollisionObject sensor;

    public Move(JsonValue jsonValue) {
        this.force = jsonValue.getFloat("force");
    }

    public Move(float force) {
        this.force = force;
    }

    private Move(Move toClone) {
        this.force = toClone.force;
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(ItemSpawner spawner, btCollisionObject parentObject) {
        this.sensor = parentObject;

    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    /*
    * NOTE: If custom Item creator ever becomes a thing move direction should be computed
    * by taking the cross product of the surface normal of the conveyors move shape.
    * This allows for conveyors to be at an incline
    *   */
    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, float timeTouching) {
        assert subject.specificData instanceof Ore;
        Ore ore = (Ore) subject.specificData;
        btRigidBody rigidBody = ore.rigidBody;

        // Get conveyor direction in world space
        ItemUserData itemUserData = (ItemUserData) source.specificData;
        Vector3 localDirection = itemUserData.direction().cpy().nor();

        Quaternion sensorRotation = new Quaternion();
        sensor.getWorldTransform().getRotation(sensorRotation);
        sensorRotation.transform(localDirection);
        Vector3 conveyorDir = localDirection.nor(); // normalized world direction

        float maxSpeed = force;          // units per second
        float responsiveness = 10f;

        Vector3 currentVel = rigidBody.getLinearVelocity().cpy();

        float velAlongDir = currentVel.cpy().dot(conveyorDir);

        float deltaV = maxSpeed - velAlongDir;

        Vector3 forceVec = conveyorDir.scl(deltaV * 10 * responsiveness);

        rigidBody.applyCentralForce(forceVec);
    }


    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    @Override
    public BodyLogic clone() {
        return new Move(this);
    }

}
