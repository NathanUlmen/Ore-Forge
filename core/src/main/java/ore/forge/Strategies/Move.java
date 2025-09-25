package ore.forge.Strategies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Screens.CollisionBehavior;

public class Move implements CollisionBehavior {
    private final float force;

    public Move(JsonValue jsonValue) {
        this.force = jsonValue.getFloat("force");
    }

    @Override
    public void interact(Fixture fixture, ItemBlueprint.ItemUserData itemUserData) {
        float direction = (itemUserData.angleOffset() + itemUserData.body().getAngle() * MathUtils.radiansToDegrees);
        direction *= MathUtils.degreesToRadians;
        float deltaTime = Gdx.graphics.getDeltaTime();
        float xForce = MathUtils.cos(direction) * force * deltaTime;
        float yForce = MathUtils.sin(direction) * force * deltaTime;

        fixture.getBody().applyForce(new Vector2(xForce, yForce), fixture.getBody().getWorldCenter(), true);
    }

    @Override
    public CollisionBehavior clone(Fixture parent) {
        return this;
    }

}
