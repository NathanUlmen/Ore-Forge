package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.utils.Disposable;
import ore.forge.GameWorld;
import ore.forge.VisualComponent;

import java.util.List;

public class EntityInstance implements Disposable {
//    private final E blueprint; //Data used to construct instance of this entity
    public Object userData; //Data that this item holds
    public List<btCollisionObject> entityPhysicsBodies; //All bodies in this object that relate to physics
    public VisualComponent visualComponent; //Visual components for this item

    //Old kept to not break project for now.
    private Body body;

    public EntityInstance(Object userData, List<btCollisionObject> physicsBodies, VisualComponent visualComponent) {
        this.userData = userData;
        this.entityPhysicsBodies = physicsBodies;
        this.visualComponent = visualComponent;
//        assert !body.isActive();
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
        for (Fixture fixture : body.getFixtureList()) {
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
        for (btCollisionObject collisionObject : entityPhysicsBodies) {
            collisionObject.dispose();
        }
        GameWorld.instance().physicsWorld().destroyBody(body);
    }

}
