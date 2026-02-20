package ore.forge.game.collisions;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import ore.forge.game.PhysicsBodyData;

public class CollisionEvent implements Pool.Poolable {
    public CollisionState type;
    public Entity a;
    public Entity b;
    public PhysicsBodyData aBody;
    public PhysicsBodyData bBody;
    public final Vector3 normalOnB = new Vector3();
    public float timeTouching;

    @Override
    public void reset() {
        timeTouching = 0;
        normalOnB.setZero();
        a = null;
        b = null;
        aBody = null;
        bBody = null;
    }
}
