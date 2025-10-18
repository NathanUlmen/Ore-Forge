package ore.forge.Strategies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.Screens.Behavior;

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
    public void update(float delta) {
        assert false;
    }

    @Override
    public void interact(Fixture fixture, ItemUserData itemUserData) {
        float direction = (itemUserData.relativeAngle() + itemUserData.body().getAngle() * MathUtils.radiansToDegrees);
        direction *= MathUtils.degreesToRadians;
        float deltaTime = Gdx.graphics.getDeltaTime();
        float xForce = MathUtils.cos(direction) * force * deltaTime;
        float yForce = MathUtils.sin(direction) * force * deltaTime;

        fixture.getBody().applyForce(new Vector2(xForce, yForce), fixture.getBody().getWorldCenter(), true);
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
