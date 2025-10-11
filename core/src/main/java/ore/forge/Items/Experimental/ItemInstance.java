package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.GameWorld;

public class ItemInstance implements Disposable {
    private final ItemBlueprint blueprint;
    private final Body body;

    public ItemInstance(ItemBlueprint blueprint, Body body) {
        this.blueprint = blueprint;
        this.body = body;
        assert !body.isActive();
    }

    //TODO: Add body to render list, register all behaviors,
    public void place(Vector2 location, float direction) {
        transform(location, direction);
        place();
        //TODO: Add body to "render list"
    }

    public void place() {
        body.setActive(true);
        //Register behaviors to their respective systems
        for (Fixture fixture : body.getFixtureList()) {
            var userData = fixture.getUserData();
            assert userData instanceof ItemUserData;
            var itemUserData = (ItemUserData) userData;
            if (itemUserData.behavior() != null) {
                itemUserData.behavior().register();
            }
        }
        //TODO: Add body to "render list"
    }

    //Removes body from gameworld, unregisters behaviors, disposes body.
    public void remove() {
        for  (Fixture fixture : body.getFixtureList()) {
            var userData = fixture.getUserData();
            assert userData instanceof ItemUserData;
            var itemUserData = (ItemUserData) userData;
            itemUserData.behavior().unregister();
        }
        dispose();
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void transform(Vector2 position, float angleDegrees) {
        body.setTransform(position.x, position.y, MathUtils.degreesToRadians * angleDegrees);
    }

    @Override
    public void dispose() {
        GameWorld.instance().physicsWorld().destroyBody(body);
    }

}
