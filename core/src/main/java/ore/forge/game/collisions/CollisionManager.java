package ore.forge.game.collisions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.utils.*;
import ore.forge.engine.components.IdComponent;
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

    private final Pool<ContactPair> pool = new Pool<>() {
        @Override protected ContactPair newObject() { return new ContactPair(); }
    };

    private final Pool<CollisionEvent> eventPool = new Pool<>() {
        @Override protected CollisionEvent newObject() { return new CollisionEvent(); }
    };

    /**
     * Call this ONCE per frame, BEFORE world.stepSimulation(...).
     * It clears the "seen this step" flag so we can detect ended subpart contacts.
     */
    public void beginFrame() {
        active.forEach(entry -> entry.value.seenThisStep = false);
    }

    /**
     * Call this ONCE per frame, AFTER world.stepSimulation(...)
     * (i.e., after Bullet has invoked onContactAdded callbacks for this step).
     */
    public void update(float delta) {
        active.forEach(entry -> {
            ContactPair pair = entry.value;

            // If this subpart pair was not reported this step, it ended
            if (!pair.seenThisStep) {
                out.addLast(createCollisionEvent(pair, CollisionState.ENDED));
                toRemove.add(entry.key);
                return;
            }

            // Otherwise it's active this step:
            switch (pair.type) {
                case STARTED -> {
                    out.addLast(createCollisionEvent(pair, CollisionState.STARTED));
                    pair.type = CollisionState.TOUCHING;
                }
                case TOUCHING -> {
                    pair.timeTouching += delta;
                    out.addLast(createCollisionEvent(pair, CollisionState.TOUCHING));
                }
                case ENDED -> {
                    // Shouldn't normally happen because we remove ENDED pairs immediately now,
                    // but keep it safe.
                    toRemove.add(entry.key);
                }
            }
        });

        for (int i = 0; i < toRemove.size; i++) {
            ContactPair removed = active.remove(toRemove.get(i));
            if (removed != null) pool.free(removed);
        }
        toRemove.clear();
    }

    @Override
    public boolean onContactAdded(btManifoldPoint cp,
                                  btCollisionObject objectA, int partId0, int index0,
                                  btCollisionObject objectB, int partId1, int index1) {

        final Entity entityA = (Entity) objectA.userData;
        final Entity entityB = (Entity) objectB.userData;
        if (entityA == null || entityB == null) return false;

        final long key = computeKey(entityA, partId0, index0, entityB, partId1, index1);
        if (key == Long.MIN_VALUE) return false;

        ContactPair pair = active.get(key);
        if (pair == null) {
            pair = pool.obtain();
            pair.type = CollisionState.STARTED;
            pair.timeTouching = 0f;

            pair.a = entityA;
            pair.b = entityB;

            pair.partIdA = partId0;
            pair.indexA  = index0;
            pair.partIdB = partId1;
            pair.indexB  = index1;

            active.put(key, pair);
        }

        // Mark as present in this step and refresh contact data
        pair.seenThisStep = true;
        cp.getNormalWorldOnB(pair.normalOnB);

        return true;
    }

    @Override
    public void onContactEnded(btCollisionObject colObj0, btCollisionObject colObj1) {
        // Intentionally ignored. End is inferred by "not seen this step".
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
            if (removed != null) pool.free(removed);
        }
        toRemove.clear();
    }

    private CollisionEvent createCollisionEvent(ContactPair pair, CollisionState state) {
        CollisionEvent event = eventPool.obtain();
        event.a = pair.a;
        event.b = pair.b;
        event.type = state;
        event.normalOnB.set(pair.normalOnB);
        event.timeTouching = pair.timeTouching;

        event.partIdA = pair.partIdA;
        event.indexA  = pair.indexA;
        event.partIdB = pair.partIdB;
        event.indexB  = pair.indexB;

        return event;
    }

    private long computeKey(Entity entityA, int partIdA, int indexA,
                            Entity entityB, int partIdB, int indexB) {

        final IdComponent a = entityA.getComponent(IdComponent.class);
        final IdComponent b = entityB.getComponent(IdComponent.class);
        if (a == null || b == null) return Long.MIN_VALUE;

        int aId = a.id;
        int bId = b.id;

        // normalize ordering + swap subshape ids with it
        if (aId > bId) {
            int tmpId = aId; aId = bId; bId = tmpId;

            int tmpP = partIdA; partIdA = partIdB; partIdB = tmpP;
            int tmpI = indexA;  indexA  = indexB;  indexB  = tmpI;
        }

        long x = 1469598103934665603L; // FNV offset basis
        x = fnv1a64(x, aId);
        x = fnv1a64(x, bId);
        x = fnv1a64(x, partIdA);
        x = fnv1a64(x, indexA);
        x = fnv1a64(x, partIdB);
        x = fnv1a64(x, indexB);
        return x;
    }

    private static long fnv1a64(long hash, int v) {
        hash ^= (v & 0xFF);
        hash *= 1099511628211L;
        hash ^= ((v >>> 8) & 0xFF);
        hash *= 1099511628211L;
        hash ^= ((v >>> 16) & 0xFF);
        hash *= 1099511628211L;
        hash ^= ((v >>> 24) & 0xFF);
        hash *= 1099511628211L;
        return hash;
    }

    private static final class ContactPair {
        public Entity a, b;
        public final Vector3 normalOnB = new Vector3();
        public float timeTouching;
        public CollisionState type;

        public int partIdA, indexA;
        public int partIdB, indexB;

        public boolean seenThisStep;
    }
}
