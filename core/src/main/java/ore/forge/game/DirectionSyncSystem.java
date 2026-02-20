package ore.forge.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.components.TransformC;

/** @author Nathan Ulmen
 *
 * Syncs the Direction of an entity whenever it is transformed.
 * Should only run when there are changes/dirty, not every frame
 *
 * - N.U Feb 19, 2026
 * */
public class DirectionSyncSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(DirectionC.class).one(TransformC.class).get();
    private static final Vector3 tmp = new Vector3();

    public DirectionSyncSystem() {
        super(FAMILY);
    }

    /**
     * */
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final DirectionC directionC = entity.getComponent(DirectionC.class);
        final TransformC transformC = entity.getComponent(TransformC.class);
        transformC.localRotation.transform(tmp);
        directionC.direction.set(tmp.add(directionC.offset).nor());
    }

}
