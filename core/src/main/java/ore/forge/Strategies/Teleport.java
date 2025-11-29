package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.EventSystem.Events.ItemPlacedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.ItemSpawner;
import ore.forge.Items.Experimental.ItemUserData;
import ore.forge.PhysicsBodyData;

public class Teleport implements BodyLogic, GameEventListener<ItemPlacedGameEvent> {
    private Fixture receiver;
    private String type;

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }


    @Override
    public void attach(ItemSpawner spawner, btCollisionObject collisionObject) {

    }


    @Override
    public void update(float delta) {

    }

    @Override
    public void onContactStart(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    @Override
    public void colliding(Object subjectData, ItemUserData userData) {
        //move contact to teleporter position
//        contact.getBody().setTransform(receiver.getBody().getTransform().getPosition(), contact.getBody().getAngle());
    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, float timeTouching) {

    }

    @Override
    public void onContactEnd(Object subjectData, ItemUserData userData) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source) {

    }

    @Override
    public BodyLogic clone() {
        return null;
    }


    @Override
    public void handle(ItemPlacedGameEvent event) {
        var placedItem = event.getSubject();
        if (placedItem instanceof Body body) {
            for (Fixture fixture : body.getFixtureList()) {
                if (fixture.getUserData() instanceof Receiver receiver) {
                    if (receiver.getType().equals(type)) {
                        this.receiver = fixture;
                    }
                }
            }

        }
    }

    @Override
    public Class<?> getEventType() {
        return null;
    }

}
