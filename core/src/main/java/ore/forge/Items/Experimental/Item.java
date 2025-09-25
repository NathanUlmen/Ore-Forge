package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Item {
    protected String name, id, description;
    private Body body;

    public void update(float deltaTime) {

    }

    public void place() {

    }

    public void remove() {

    }


    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void rotate(float angleDegrees) {
        body.setTransform(body.getPosition().x, body.getPosition().y, MathUtils.degreesToRadians * angleDegrees);
    }

    public void transform(Vector2 position, float angleDegrees) {
        body.setTransform(position.x, position.y, MathUtils.degreesToRadians * angleDegrees);
    }

}
