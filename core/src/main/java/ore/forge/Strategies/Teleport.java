package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import ore.forge.EventSystem.Events.ItemPlacedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.Items.Experimental.ItemBlueprint;
import ore.forge.Screens.CollisionBehavior;

public class Teleport implements CollisionBehavior, GameEventListener<ItemPlacedGameEvent> {
    private Fixture receiver;
    private String type;

    @Override
    public void interact(Fixture contact, ItemBlueprint.ItemUserData userData) {
        //move contact to teleporter position
        contact.getBody().setTransform(receiver.getBody().getTransform().getPosition(), contact.getBody().getAngle());
    }

    @Override
    public CollisionBehavior clone(Fixture parent) {
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
