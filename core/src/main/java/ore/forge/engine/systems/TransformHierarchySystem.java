package ore.forge.engine.systems;



import com.badlogic.ashley.core.*;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.components.*;


/**
 * Builds derived world transforms {@link WorldTransformC} from canonical local transforms {@link TransformC}.
 * Only goes one level Deep at the moment
 * <p>
 * - N.U. Feb 23, 2026
 */
public class TransformHierarchySystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(WorldTransformC.class, ChildC.class, TransformC.class).get();

    private final Matrix4 tmp = new Matrix4();
    private final Matrix4 tmpLocal = new Matrix4();

    public TransformHierarchySystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        //We want to start with the root
        final Entity root = getRootEntity(entity);
        final ParentC parent = root.getComponent(ParentC.class);
        final WorldTransformC rootWorld = root.getComponent(WorldTransformC.class);

        for (Entity child : parent.children) {
            final WorldTransformC childWorld = child.getComponent(WorldTransformC.class);
            final TransformC localTransform = child.getComponent(TransformC.class);

            //Child world = local canonical transform relative to its parent.
            localTransform.toLocalMatrix(tmpLocal);
            tmp.set(rootWorld.currentTransform);

            tmp.mul(tmpLocal);

            childWorld.advance();
            childWorld.currentTransform.set(tmp);

            final PhysicsC physicsC = child.getComponent(PhysicsC.class);
            if (physicsC != null) {
                physicsC.collisionObject.setWorldTransform(tmp);
            }
        }

    }

    private Entity getRootEntity(Entity entity) {
        Entity parent = entity;
        while (true) {
            final ChildC childC = parent.getComponent(ChildC.class);
            if (childC != null && childC.parent != null) {
                parent = childC.parent;
            } else {
                break;
            }
        }
        return parent;
    }

}

