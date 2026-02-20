package ore.forge.game.collisions;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Array;
import ore.forge.game.GameContext2;

public class PhysicsCallbackResolverSystem extends EntitySystem {
    private static final ComponentMapper<CollisionHandlerC> HANDLERS = ComponentMapper.getFor(CollisionHandlerC.class);
    private final CollisionManager collisionManager;
    private final GameContext2 context;
    private final Array<CollisionEvent> collisionEvents = new Array<>(false, 128);

    public PhysicsCallbackResolverSystem(GameContext2 context, CollisionManager collisionManager) {
        this.collisionManager = collisionManager;
        this.context = context;

    }

    @Override
    public void update(float delta) {
        collisionManager.drainTo(collisionEvents);
        for (CollisionEvent event : collisionEvents) {
            fire(event.a, event.b, event);
            fire(event.b, event.a, event);
            collisionManager.free(event);
        }
        collisionEvents.clear();
    }

    private void fire(Entity self, Entity other, CollisionEvent event) {
        if (self.isScheduledForRemoval() || other.isScheduledForRemoval()) return;
        CollisionHandlerC logic = HANDLERS.get(self);
        if (logic == null) return;
        switch (event.type) {
            case STARTED -> {
                for (var contactStartedLogic : logic.starts) {
                    contactStartedLogic.onStart(self, other, event, context);
                }
            }
            case TOUCHING -> {
                for (var collidingLogic : logic.touchings) {
                    collidingLogic.onTouching(self, other, event, context);
                }
            }
            case ENDED -> {
                for (var contactEndedLogic : logic.ended) {
                    contactEndedLogic.onEnd(self, other, event, context);
                }
            }
        }
    }

}
