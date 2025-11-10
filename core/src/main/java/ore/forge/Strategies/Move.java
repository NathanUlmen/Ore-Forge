package ore.forge.Strategies;

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

    public Move(JsonValue jsonValue) {
        this.force = jsonValue.getFloat("force");
    }

    public Move(float force) {
        this.force = force;
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(Body body, Fixture fixture) {
        fixture.setUserData(this);
    }

    @Override
    public void attach(UpgraderSpawner spawner, btCollisionObject parentObject) {

    }

    @Override
    public void update(float delta) {
        assert false;
    }

    @Override
    public void interact(Object subjectData, ItemUserData itemUserData) {
        assert subjectData instanceof Ore;
        Ore ore = (Ore) subjectData;
        btRigidBody rigidBody = ore.rigidBody;
        rigidBody.applyCentralImpulse(itemUserData.direction().cpy().nor().scl(force));
    }

    @Override
    public Behavior clone(Fixture parent) {
        return this;
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
    }

}
