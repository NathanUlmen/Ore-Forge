package ore.forge.game.collisions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import ore.forge.game.PhysicsBodyData;

public final class CollisionEvent {
    public Entity a;
    public Entity b;

    public CollisionState type;
    public final Vector3 normalOnB = new Vector3();
    public float timeTouching;

    // Bullet sub-shape identifiers:
    public int partIdA;
    public int indexA;

    public int partIdB;
    public int indexB;

    public int indexFor(Entity self) {
        if (self == a) return indexA;
        if (self == b) return indexB;
        return -1;
    }

    public int partIdFor(Entity self) {
        if (self == a) return partIdA;
        if (self == b) return partIdB;
        return -1;
    }
}
