package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.WorldTransformC;

/**
 * This System sets prev pos, rot, and scale for interpolation purposes.
 * */
public class PrePhysicsTransformSyncSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(
        WorldTransformC.class, PhysicsC.class).get();

    public PrePhysicsTransformSyncSystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final PhysicsC physics = entity.getComponent(PhysicsC.class);
        if (physics.type != PhysicsC.BodyType.KINEMATIC) return;

        final WorldTransformC world = entity.getComponent(WorldTransformC.class);
        physics.rigidBody.setWorldTransform(world.currentTransform);
        physics.rigidBody.activate();
    }

}
