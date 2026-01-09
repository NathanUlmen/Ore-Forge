package ore.forge.Strategies;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.EventSystem.Events.ItemPlacedGameEvent;
import ore.forge.EventSystem.GameEventListener;
import ore.forge.GameState;
import ore.forge.Items.ItemDefinition;
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
    public void attach(ItemDefinition definition, btCollisionObject collisionObject) {

    }


    @Override
    public void update(float delta) {

    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameState state) {

    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameState state, float timeTouching) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameState state) {

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
