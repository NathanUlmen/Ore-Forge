package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.PhysicsMotionType;
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
        if (physics.motionType == PhysicsMotionType.DYNAMIC) {
            world.advance();
            world.currentTransform.set(physics.collisionObject.getWorldTransform());
        }
    }

    protected void processEntity(Entity entity, float deltaTime, int priority) {
    }

}

