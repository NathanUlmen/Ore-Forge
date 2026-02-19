package ore.forge.engine.systems;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.Entity;
import ore.forge.engine.PhysicsBody;
import ore.forge.engine.components.PhysicsC;
import ore.forge.engine.components.PhysicsComponent;
import ore.forge.engine.components.RenderComponent;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.render.RenderPart;

/**
 * @author Nathan Ulmen
 * Transform manager is responsible for syncing the transforms of Entities across thier
 * different components. At the time of writing that means that it handles
 * {@link ore.forge.engine.components.PhysicsC}, {@link Entity}, and {@link ore.forge.engine.components.RenderC}
 * - N.U Feb 17, 2026.
 *
 */
public class TransformManager {
    //Thread local mats to avoid allocations
    private static final ThreadLocal<Matrix4> TMP_A = ThreadLocal.withInitial(Matrix4::new);
    private static final ThreadLocal<Matrix4> TMP_B = ThreadLocal.withInitial(Matrix4::new);
    private static final ThreadLocal<Matrix4> TMP_C = ThreadLocal.withInitial(Matrix4::new);

    /**
     * Pre physics tick
     */
    public static void preTickSync(Entity entity) {
        PhysicsC physics = entity.physicsComponent;
        RenderC visuals = entity.renderC;
        //update root transform if applicable
        PhysicsBody driverBody = physics.getDriverBody();
        if (driverBody != null) {
            entity.rootTransform.advance();
        }

        //set our previous transforms
        if (visuals != null) {
            for (RenderComponent renderComp : visuals.renderComponents) {
                if (renderComp.drivenByBody != -1) { //if driven by render
                    renderComp.localFromBody.advance();
                }
            }
        }

//        if (physics == null || visuals == null) return;
//
//        //sync bodies with root transform.
//        for (PhysicsBody body : physics.bodies) {
//            switch (body.bodyType) {
//                case KINEMATIC, STATIC -> {
//
//                }
//                case DYNAMIC -> {
//                    //do nothing
//                }
//            }
//        }


    }

    public static void postTickSync(Entity entity) {
        PhysicsComponent physicsComponent = entity.physicsComponent;
        PhysicsBody driverBody = entity.physicsComponent.getDriverBody();
        if (driverBody != null) {
            entity.rootTransform.currentTransform.set(driverBody.bodyHandle.getWorldTransform());
        }

        RenderC visuals = entity.renderC;
        if (visuals == null) return;
        //Set our current transforms
        for (RenderComponent renderComp : entity.renderC.renderComponents) {
            if (renderComp == null) continue;
            if (renderComp.drivenByBody != -1) {
                PhysicsBody drivenBy = physicsComponent.bodies.get(renderComp.drivenByBody);
                renderComp.localFromBody.currentTransform.set(drivenBy.bodyHandle.getWorldTransform());
            }
        }

    }

    /**
     * This is where we finalize RenderParts before shipping them of to render.
     * */
    public static void preRender(Entity entity, float alpha) {
        RenderC renderC = entity.renderC;
        if (renderC == null) return;

        for (RenderComponent renderComponent : renderC.renderComponents) {
            RenderPart part = renderComponent.renderPart;
            if (renderComponent.drivenByBody != -1 && renderComponent.localFromBody != null) { //if tied to body lerp, handles Dynamic
                //Sync to drivenBy
                part.transform.set(renderComponent.localFromBody.lerp(alpha));
                continue;
            }

            //Handles static and kinematic.
            part.transform.set(entity.rootTransform.lerp(alpha).mul(renderComponent.localFromRoot));

        }

    }

    public static void teleport(Entity entity, Matrix4 targetRootWorld) {
        if (entity == null || targetRootWorld == null) return;

        //Snap root history
        if (entity.rootTransform != null) {
            entity.rootTransform.setBoth(targetRootWorld);
        }

        PhysicsComponent physics = entity.physicsComponent;
        if (physics != null && physics.bodies != null) {
            Matrix4 tmpWorld = TMP_A.get();
            for (PhysicsBody b : physics.bodies) {
                if (b == null || b.bodyHandle == null) continue;

                // bodyWorld = targetRootWorld * localFromRoot
                tmpWorld.set(targetRootWorld);
                if (b.localFromRoot != null) tmpWorld.mul(b.localFromRoot);
                b.bodyHandle.setWorldTransform(tmpWorld);
            }
        }

        // Snap body driven histories used for rendering
        RenderC visual = entity.renderC;
        if (visual != null && physics != null && physics.bodies != null) {
            for (RenderComponent rc : visual.renderComponents) {
                if (rc == null || rc.localFromBody == null) continue;
                if (rc.drivenByBody == -1) continue;

                int idx = rc.drivenByBody;
                if (idx < 0 || idx >= physics.bodies.size()) continue;

                PhysicsBody b = physics.bodies.get(idx);
                if (b == null || b.bodyHandle == null) continue;

                Matrix4 world = b.bodyHandle.getWorldTransform();
                rc.localFromBody.setBoth(world);
            }
        }

    }

}
