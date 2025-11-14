package ore.forge.Strategies;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Ore;

public class Move implements Behavior {
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
    public void onContactStart(Object subjectData, ItemUserData userData) {
        System.out.println("Move Contact Started");
    }

    @Override
    public void colliding(Object subjectData, ItemUserData itemUserData) {
        assert subjectData instanceof Ore;
        Ore ore = (Ore) subjectData;
        btRigidBody rigidBody = ore.rigidBody;

        // 1. Get sensor rotation (for force direction)
        Quaternion sensorRotation = new Quaternion();
        sensor.getWorldTransform().getRotation(sensorRotation);

        // 2. Rotate direction into world space
        Vector3 worldDirection = itemUserData.direction().cpy();
        sensorRotation.transform(worldDirection);

        Vector3 forceVec = worldDirection.nor().scl(force);

        // 3. Local-space offset where the force is applied
        Vector3 localOffset = new Vector3(0.5f, 0f, 0f);  // example: right side of the ore

        // 4. Convert local offset → world offset (relative to the rigidbody)
        Vector3 worldOffset = localOffset.cpy();
        rigidBody.getWorldTransform().getRotation(new Quaternion()).transform(worldOffset);

        // 5. Apply force at that relative world-space offset
        rigidBody.applyForce(forceVec, worldOffset);
    }



    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {

    }

    @Override
    public Behavior clone() {
        return new Move(this);
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
    }

}
