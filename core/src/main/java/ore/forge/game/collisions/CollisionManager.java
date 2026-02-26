package ore.forge.game.collisions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.*;
import ore.forge.engine.components.IdComponent;

/**
 * @author Nathan Ulmen
 * The CollisionManager class is responsible for producing and tracking the state
 * of ContactStates. It tracks which state the ContactState is in (STARTED, TOUCHING, ENDED)
 * and the time those bodies have been touching.
 * <p>
 * It is responsible for pushing CollisionEvents out for the CollisionCallbackSystem
 * to resolve/process.
 * <p>
 * The pipeline for this thing looks like this:
 * Physics step runs, Collision manager records all callbacks
 * Update Callbacks
 * produce CollisionEvents.
 * - N.U Feb 19, 2026
 *
 */
public class CollisionManager extends ContactListener {
    private final LongMap<ContactPair> active = new LongMap<>();
    private final Queue<CollisionEvent> out = new Queue<>();
    private final LongArray toRemove = new LongArray(false, 64);

    private final Pool<ContactPair> pairPool = new Pool<>() {
        @Override
        protected ContactPair newObject() {
            return new ContactPair();
        }
    };

    private final Pool<CollisionEvent> eventPool = new Pool<>() {
        @Override
        protected CollisionEvent newObject() {
            return new CollisionEvent();
        }
    };

    /*
     * Called after physics step
     * */
    public void update(float delta) {
        System.out.println(active.size);
        active.forEach(entry -> {
            ContactPair pair = entry.value;
            switch (pair.state) {
                case STARTED -> {
                    out.addLast(createCollisionEvent(pair, CollisionState.STARTED));
                    pair.state = CollisionState.TOUCHING;
                }
                case TOUCHING -> {
                    pair.timeTouching += delta;
                    out.addLast(createCollisionEvent(pair, CollisionState.TOUCHING));
                }
                case ENDED -> {
                    toRemove.add(entry.key);
                }
            }
        });


        for (int i = 0; i < toRemove.size; i++) {
            ContactPair removed = active.remove(toRemove.get(i));
            if (removed != null) pairPool.free(removed);
        }
        toRemove.clear();
    }

    @Override
    public void onContactStarted(btCollisionObject o1, btCollisionObject o2) {
        final Entity a = (Entity) o1.userData;
        final Entity b = (Entity) o2.userData;
        if (a == null || b == null) {
            Gdx.app.error("CollisionManager", "Entity is null collisionStarted!");
            return;
        }

        long key = computeKey(a, b);
        if (key == Long.MIN_VALUE) {return;}
        ContactPair pair = pairPool.obtain();
        pair.a = a;
        pair.b = b;
        pair.timeTouching = 0f;
        pair.state = CollisionState.STARTED;
        active.put(key, pair);
    }

    @Override
    public void onContactEnded(btCollisionObject o1, btCollisionObject o2) {
        final Entity a = (Entity) o1.userData;
        final Entity b = (Entity) o2.userData;
        if (a == null || b == null) {
            Gdx.app.error("CollisionManager", "Entity is null collisionEnded!");
            return;
        }

        long key = computeKey(a, b);
        if (key == Long.MIN_VALUE) {return;}
        active.get(key).state = CollisionState.ENDED;
    }

    public void drainTo(Array<CollisionEvent> destination) {
        while (!out.isEmpty()) destination.add(out.removeFirst());
    }

    public void free(CollisionEvent collisionEvent) {
        eventPool.free(collisionEvent);
    }

    public void removeAllPairsWith(Entity entity) {
        active.forEach(entry -> {
            ContactPair pair = entry.value;
            if (pair.a == entity || pair.b == entity) toRemove.add(entry.key);
        });

        for (int i = 0; i < toRemove.size; i++) {
            ContactPair removed = active.remove(toRemove.get(i));
            if (removed != null) pairPool.free(removed);
        }
        toRemove.clear();
    }

    private CollisionEvent createCollisionEvent(ContactPair pair, CollisionState state) {
        CollisionEvent event = eventPool.obtain();
        event.a = pair.a;
        event.b = pair.b;
        event.type = state;
        event.timeTouching = pair.timeTouching;

        return event;
    }

    private long computeKey(Entity entityA,
                            Entity entityB) {

        final IdComponent a = entityA.getComponent(IdComponent.class);
        final IdComponent b = entityB.getComponent(IdComponent.class);
        if (a == null || b == null) return Long.MIN_VALUE;


        int min = Math.min(a.id, b.id);
        int max = Math.max(a.id, b.id);

        return ((long) min << 32) | (max & 0xFFFFFFFFL);
    }

    private static final class ContactPair {
        public Entity a, b;
        public float timeTouching;
        public CollisionState state;
    }
}
