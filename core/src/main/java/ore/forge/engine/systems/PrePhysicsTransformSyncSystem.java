package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.WorldTransformC;


/**
 * This class is responsible for setting previousTransformation
 * to current before physics tick
 *
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
        final WorldTransformC world = entity.getComponent(WorldTransformC.class);

        world.advance();
        switch (physics.type) {
            case KINEMATIC -> {
                physics.rigidBody.setWorldTransform(world.currentTransform);
                physics.rigidBody.activate();
            }
            case DYNAMIC -> {
            }
        }

    }

}
