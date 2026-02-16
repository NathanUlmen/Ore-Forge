package ore.forge.engine.Components;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.render.RenderPart;

public interface PhysicsCOmp {

    /**
     * Takes the transform from a render part and sets it to match its own.
     * must be called every
     * */
    void syncRenderTransform(Matrix4 toOperateOn);

    /**
     * Takes the entity central transform and updates it/syncs it from physics.
     * Must be called after every physics step. Intended for dynamic objects.
     */
    void syncEntityTransform(Matrix4 toOperateOn);

    /**
     * Used to set position of an entity
     * */
    void setTransform(Matrix4 newTransform);

    void addToWorld();

}
