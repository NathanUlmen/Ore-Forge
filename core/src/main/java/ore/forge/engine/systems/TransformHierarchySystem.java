package ore.forge.engine.systems;

import com.badlogic.ashley.core.EntitySystem;


import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.components.*;

import java.util.IdentityHashMap;

/**
 * Builds derived WORLD transforms (WorldTransformC) from canonical LOCAL transforms (TransformC),
 * respecting parent-child relationships.
 * <p>
 * Assumptions:
 * - TransformC stores LOCAL TRS (relative to parent if inheriting; roots local==world).
 * - WorldTransformC is a derived cache: current + previous matrices.
 * - ParentC.children is maintained elsewhere (or by a separate hierarchy maintenance system).
 * <p>
 * Order:
 * - (optional) a system that calls TransformC.advance() once per tick
 * - TransformHierarchySystem (this) -> writes WorldTransformC.current (and advances previous)
 * - Physics sync / Render systems read WorldTransformC
 */
public class TransformHierarchySystem extends EntitySystem {

    private static final Family WORLD_FAMILY =
        Family.all(TransformC.class, WorldTransformC.class).get();

    private static final Family ROOT_FAMILY =
        // Root = has Transform+World and either no Child, or ChildC.parent == null, or inheritTransform == false
        Family.all(TransformC.class, WorldTransformC.class).get();

    private ImmutableArray<Entity> allWithWorld;
    private ImmutableArray<Entity> potentialRoots;

    private final ComponentMapper<TransformC> tm = ComponentMapper.getFor(TransformC.class);
    private final ComponentMapper<WorldTransformC> wm = ComponentMapper.getFor(WorldTransformC.class);
    private final ComponentMapper<ParentC> pm = ComponentMapper.getFor(ParentC.class);
    private final ComponentMapper<ChildC> cm = ComponentMapper.getFor(ChildC.class);

    //avoid allocations
    private final Matrix4 tmpLocal = new Matrix4();
    private final Matrix4 tmpWorld = new Matrix4();

    //cycle protection
    private final IdentityHashMap<Entity, Boolean> visiting = new IdentityHashMap<>();

    @Override
    public void addedToEngine(Engine engine) {
        allWithWorld = engine.getEntitiesFor(WORLD_FAMILY);
        potentialRoots = engine.getEntitiesFor(ROOT_FAMILY);
    }

    @Override
    public void update(float deltaTime) {
        // Advance world history
        for (int i = 0; i < allWithWorld.size(); i++) {
            final WorldTransformC w = wm.get(allWithWorld.get(i));
            w.previousTransform.set(w.currentTransform);
        }

        // Traverse from roots
        visiting.clear();

        for (int i = 0; i < potentialRoots.size(); i++) {
            final Entity e = potentialRoots.get(i);

            final ChildC child = cm.get(e);
            final boolean isRoot = (child == null) || (child.parent == null) || !child.inheritTransform;

            if (!isRoot) continue;

            // rootWorld = rootLocal (since root local == world)
            final TransformC t = tm.get(e);
            final WorldTransformC w = wm.get(e);

            t.toLocalMatrix(tmpLocal);
            w.currentTransform.set(tmpLocal);

            // recurse children
            final ParentC p = pm.get(e);
            if (p != null) {
                for (Entity c : p.children) {
                    updateChildRecursive(c, w.currentTransform);
                }
            }
        }
    }

    private void updateChildRecursive(Entity childEntity, Matrix4 parentWorld) {
        if (childEntity == null) return;

        if (visiting.put(childEntity, Boolean.TRUE) != null) {
            return; // already visiting so return
        }

        final TransformC t = tm.get(childEntity);
        final WorldTransformC w = wm.get(childEntity);

        if (t == null || w == null) {
            visiting.remove(childEntity);
            return;
        }

        final ChildC child = cm.get(childEntity);
        final boolean inherit = (child != null) && child.inheritTransform && (child.parent != null);

        // Build local matrix
        t.toLocalMatrix(tmpLocal);

        if (inherit) {
            // childWorld = parentWorld * childLocal
            tmpWorld.set(parentWorld).mul(tmpLocal);
            w.currentTransform.set(tmpWorld);
        } else {
            // Treat local as world when not inheriting
            w.currentTransform.set(tmpLocal);
        }

        // Recurse further
        final ParentC p = pm.get(childEntity);
        if (p != null) {
            for (Entity gc : p.children) {
                updateChildRecursive(gc, w.currentTransform);
            }
        }

        visiting.remove(childEntity);
    }
}

