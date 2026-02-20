package ore.forge.game.collisions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.*;
import ore.forge.engine.components.IdComponent;
import ore.forge.game.CollisionSystem;
import ore.forge.game.PhysicsBodyData;

/**
 * @author Nathan Ulmen
 * The CollisionManager class is responsible for producing and tracking the state
 * of ContactStates. It tracks which state the ContactState is in (STARTED, TOUCHING, ENDED)
 * and the time those bodies have been touching.
 * <p>
 * It is responsible for pushing CollisionEvents out for the CollisionCallbackSystem
 * to resolve/process.
 * <p>
 *
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

    private final Pool<ContactPair> pool = new Pool<ContactPair>() {
        @Override
        protected ContactPair newObject() {
            return new ContactPair();
        }
    };
    private final Pool<CollisionEvent> eventPool = new  Pool<CollisionEvent>() {
        @Override
        protected CollisionEvent newObject() {
            return new  CollisionEvent();
        }
    };


    public CollisionManager() {
    }

    public void update(float delta) {
        active.forEach(contactState -> {
            ContactPair state = contactState.value;
            switch (state.type) {
                case STARTED -> {
                    CollisionEvent event = createCollisionEvent(state, CollisionState.STARTED);
                    out.addLast(event);
                    //after being processed once its now touching.
                    state.type = CollisionState.TOUCHING;

                }
                case TOUCHING -> {
                    state.timeTouching += delta;
                    CollisionEvent event = createCollisionEvent(state, CollisionState.TOUCHING);
                    out.addLast(event);
                }
                case ENDED -> {
                    CollisionEvent event = createCollisionEvent(state,  CollisionState.ENDED);
                    out.addLast(event);
                    toRemove.add(contactState.key);
                }
            }
        });
        for (int i = 0; i < toRemove.size; i++) {
            ContactPair removed = active.remove(toRemove.get(i));
            pool.free(removed);
        }
        toRemove.clear();
    }

    /**
     * onContactAdded fires each can fire multiple times
     *
     * */
    @Override
    public boolean onContactAdded(btManifoldPoint cp, btCollisionObject objectA, int partId0, int index0, btCollisionObject objectB, int partId1, int index1) {
        final PhysicsBodyData a = (PhysicsBodyData) objectA.userData;
        final PhysicsBodyData b = (PhysicsBodyData) objectB.userData;
        if (a == null || b == null) return false;

        final Entity entityA = a.entity();
        final Entity entityB = b.entity();
        if (entityA == null || entityB == null) return false;

        final long key = computeKey(entityA, entityB);
        if (key == -1L) return false;

        ContactPair state = active.get(key);
        if (state == null) {
            state = pool.obtain();
            state.type = CollisionState.STARTED;
            state.timeTouching = 0f;
            state.a = a;
            state.b = b;

            cp.getNormalWorldOnB(state.normalOnB);

            active.put(key, state);
        } else {
            //keep latest normal updated while touching
            cp.getNormalWorldOnB(state.normalOnB);
        }

        return true;
    }

    @Override
    public void onContactEnded(btCollisionObject colObj0, btCollisionObject colObj1) {
        PhysicsBodyData a = (PhysicsBodyData) colObj0.userData;
        PhysicsBodyData b = (PhysicsBodyData) colObj1.userData;
        long key = computeKey(a.entity(), b.entity());
        if (key < 0) return;
        ContactPair state = active.get(key);
        if (state == null) return;
        state.type = CollisionState.ENDED;
    }

    public void drainTo(Array<CollisionEvent> destination) {
        while (!out.isEmpty()) {
            destination.add(out.removeFirst());
        }
    }

    private CollisionEvent createCollisionEvent(ContactPair pair, CollisionState state) {
        CollisionEvent event = eventPool.obtain();
        event.a = pair.a.entity();
        event.b = pair.b.entity();
        event.type = state;
        event.normalOnB.set(pair.normalOnB);
        event.timeTouching = pair.timeTouching;
        return event;
    }

    private long computeKey(Entity entityA, Entity entityB) {
        final IdComponent a = entityA.getComponent(IdComponent.class);
        final IdComponent b = entityB.getComponent(IdComponent.class);
        if (a == null || b == null) return -1;
        int low = Math.min(a.id, b.id);
        int high = Math.max(a.id, b.id);
        return ((long) low << 32) | (high & 0xFFFFFFFFL);
    }

    public void free(CollisionEvent collisionEvent) {
        eventPool.free(collisionEvent);
    }

    private static final class ContactPair {
        public PhysicsBodyData a, b;
        public final Vector3  normalOnB = new Vector3();
        public float timeTouching;
        public CollisionState type;
    }

}
