package ore.forge.Items.Experimental;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.Collision;
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

    public EntityInstance(Object userData, List<btCollisionObject> collisionObjects, VisualComponent visualComponent) {
        this.userData = userData;
        this.entityPhysicsBodies = collisionObjects;
        this.visualComponent = visualComponent;

//        assert !body.isActive();
    }

    //TODO: Add body to render list, register all behaviors,
    public void place(Vector3 location, float direction) {
        Matrix4 transform = new Matrix4();
        transform.setTranslation(location);
        transform.setToRotation(Vector3.Y,  direction);
        this.setTransform(transform);
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

    //Should only be used for the initial placement of an item
    public void setTransform(Matrix4 transform) {
        for (btCollisionObject object : entityPhysicsBodies) {
            object.setWorldTransform(transform);
        }
        ModelInstance modelInstance = visualComponent.modelInstance;
        modelInstance.transform.set(transform);
        modelInstance.calculateTransforms();
    }

    @Override
    public void dispose() {
        for (btCollisionObject collisionObject : entityPhysicsBodies) {
            collisionObject.dispose();
        }
        GameWorld.instance().physicsWorld().destroyBody(body);
    }

}
