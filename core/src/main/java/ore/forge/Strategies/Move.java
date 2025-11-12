package ore.forge.Strategies;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Items.Experimental.UpgraderSpawner;
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
    public void attach(UpgraderSpawner spawner, btCollisionObject parentObject) {
        this.sensor = parentObject;

    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void onContactStart(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void colliding(Object subjectData, ItemUserData itemUserData) {
        assert subjectData instanceof Ore;
        Ore ore = (Ore) subjectData;
        btRigidBody rigidBody = ore.rigidBody;
        Quaternion quaternion = new Quaternion();
        sensor.getWorldTransform().getRotation(quaternion);
        var newDirection = itemUserData.direction().cpy();
        quaternion.transform(newDirection);
        rigidBody.applyCentralForce(newDirection.nor().scl(force));
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
