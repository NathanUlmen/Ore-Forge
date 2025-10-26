package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import ore.forge.EventSystem.Events.ItemPlacedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.ItemUserData;

public class Teleport implements Behavior, GameEventListener<ItemPlacedGameEvent> {
    private Fixture receiver;
    private String type;

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    @Override
    public void attach(Body body, Fixture fixture) {

    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void interact(Object subjectData, ItemUserData userData) {
        //move contact to teleporter position
//        contact.getBody().setTransform(receiver.getBody().getTransform().getPosition(), contact.getBody().getAngle());
    }

    @Override
    public Behavior clone(Fixture parent) {
        return null;
    }

    @Override
    public boolean isCollisionBehavior() {
        return true;
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
