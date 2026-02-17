package ore.forge.engine.systems;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.Entity;
import ore.forge.engine.PhysicsBody;
import ore.forge.engine.components.PhysicsComponent;
import ore.forge.engine.components.RenderComponent;
import ore.forge.engine.components.VisualComponent;
import ore.forge.engine.render.RenderPart;

/**
 * @author Nathan Ulmen
 * Transform manager is responsible for syncing the transforms of Entities across thier
 * different components. At the time of writing that means that it handles
 * {@link ore.forge.engine.components.PhysicsComponent}, {@link Entity}, and {@link ore.forge.engine.components.RenderComponent}
 * - N.U Feb 17, 2026.
 *
 */
public class TransformManager {

    /**
     * Pre physics tick
     *
     */
    public static void preTickSync(Entity entity) {
        PhysicsComponent physicsComponent = entity.physicsComponent;
        //update root transform if applicable
        PhysicsBody driverBody = physicsComponent.getDriverBody();
        if (driverBody != null) {
            entity.rootTransform.previousTransform.set(driverBody.bodyHandle.getWorldTransform());
        }

        //set our previous transforms
        for (RenderComponent renderComp : entity.visualComponent.renderComponents) {
            if (renderComp.drivenByBody != -1) {
                PhysicsBody drivenBy = physicsComponent.bodies.get(renderComp.drivenByBody);
                renderComp.localFromBody.previousTransform.set(drivenBy.bodyHandle.getWorldTransform());
            }
        }

    }

    public static void postTickSync(Entity entity) {
        PhysicsComponent physicsComponent = entity.physicsComponent;
        PhysicsBody driverBody = entity.physicsComponent.getDriverBody();
        if (driverBody != null) {
            entity.rootTransform.currentTransform.set(driverBody.bodyHandle.getWorldTransform());
        }

        //Set our current transforms
        for (RenderComponent renderComp : entity.visualComponent.renderComponents) {
            if (renderComp.drivenByBody != -1) {
                PhysicsBody drivenBy = physicsComponent.bodies.get(renderComp.drivenByBody);
                renderComp.localFromBody.currentTransform.set(drivenBy.bodyHandle.getWorldTransform());
            }
        }

    }

    //Here is where we initialize our RenderParts
    public static void preRender(Entity entity, float alpha) {
        VisualComponent visualComponent = entity.visualComponent;

        for (RenderComponent renderComponent : visualComponent.renderComponents) {
            RenderPart part = renderComponent.renderPart;
            if (renderComponent.drivenByBody != -1) { //if tied to body lerp, handles Dynamic
                //Sync to drivenBy
                part.transform.set(renderComponent.localFromBody.lerp(alpha));
                continue;
            }

            //Handles static and kinematic.
            part.transform.set(entity.rootTransform.lerp(alpha).mul(renderComponent.localFromRoot));

        }

    }

    public static void teleport(Entity entity, Matrix4 target) {

    }

}
