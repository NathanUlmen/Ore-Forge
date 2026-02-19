package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.TransformC;
import ore.forge.engine.components.WorldTransformC;

public class PostPhysicsTransformSyncSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(
        WorldTransformC.class, PhysicsC.class).get();

    public PostPhysicsTransformSyncSystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final WorldTransformC world = entity.getComponent(WorldTransformC.class);
        final PhysicsC physics = entity.getComponent(PhysicsC.class);
        if (physics.type == PhysicsC.BodyType.DYNAMIC) {
            world.currentTransform.set(physics.rigidBody.getWorldTransform());
        }
    }
}
