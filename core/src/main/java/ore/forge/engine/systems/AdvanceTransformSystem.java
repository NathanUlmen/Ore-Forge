package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import ore.forge.engine.components.TransformC;

public class AdvanceTransformSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(TransformC.class).get();

    public AdvanceTransformSystem(Family family) {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        entity.getComponent(TransformC.class).advance();
    }

}
