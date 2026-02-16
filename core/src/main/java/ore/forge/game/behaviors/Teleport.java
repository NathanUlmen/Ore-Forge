package ore.forge.game.behaviors;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import ore.forge.game.event.Events.ItemPlacedGameEvent;
import ore.forge.game.event.GameEventListener;
import ore.forge.game.GameContext;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.PhysicsBodyData;

public class Teleport implements BodyLogic, GameEventListener<ItemPlacedGameEvent> {
    private Fixture receiver;
    private String type;

    @Override
    public void register(GameContext context) {

    }

    @Override
    public void unregister(GameContext context) {

    }


    @Override
    public void attach(ItemDefinition definition, btCollisionObject collisionObject) {

    }


    @Override
    public void update(float delta) {

    }

    @Override
    public void onContactStart(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

    }

    @Override
    public void colliding(PhysicsBodyData subject, PhysicsBodyData source, GameContext context, float timeTouching) {

    }

    @Override
    public void onContactEnd(PhysicsBodyData subject, PhysicsBodyData source, GameContext context) {

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
