package ore.forge.engine.systems;

import ore.forge.engine.components.DynamicPhysicsComponent;
import ore.forge.engine.components.StaticPhysicsComponent;
import ore.forge.engine.EntityInstance;

/** @author Nathan Ulmen
 * Transform manager is responsible for syncing the transforms of Entities across thier
 * different components. At the time of writing that means that it handles
 * {@link ore.forge.engine.components.PhysicsComp} implementations, {@link EntityInstance}, and {@link ore.forge.engine.render.RenderPart}
 * - N.U Feb 17, 2026.
 * */
public class TransformManager {

    /**
     *
     * */
    public static void preTickSync(EntityInstance entity) {
        switch (entity.physicsComp) {
            case DynamicPhysicsComponent dynamicComponent -> {

            }
            case StaticPhysicsComponent staticComponent-> {

            }
        }
    }

    public static void postTickSync(EntityInstance entity) {
        switch (entity.physicsComp) {
            case DynamicPhysicsComponent dynamicComponent -> {

            }
            case StaticPhysicsComponent staticComponent-> {

            }
        }
    }

}
