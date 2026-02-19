package ore.forge.engine.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.components.RenderC;
import ore.forge.engine.components.WorldTransformC;
import ore.forge.engine.render.RenderPart;

/**
 * This systems job is to configure the transform of RenderPart
 * before we ship it of to the renderer.
 * This means we need to interpolate where needed.
 *
 * */
public class RenderPrepSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(RenderC.class).one(WorldTransformC.class).get();
    private static final Matrix4 tmp = new Matrix4();
    private float alpha = 1f;
    private final Vector3 p0 = new Vector3();
    private final Vector3 p1 = new Vector3();
    private final Vector3 s0 = new Vector3();
    private final Vector3 s1 = new Vector3();
    private final Vector3 p = new Vector3();
    private final Vector3 s = new Vector3();
    private final Quaternion r0 = new Quaternion();
    private final Quaternion r1 = new Quaternion();
    private final Quaternion r = new Quaternion();
    private final Matrix4 interpWorld = new Matrix4();


    public RenderPrepSystem() {
        super(FAMILY);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        final WorldTransformC worldTransform = (WorldTransformC) entity.getComponent(WorldTransformC.class);
        final RenderC renderC = entity.getComponent(RenderC.class);

        interpolateTRS(interpWorld, worldTransform.previousTransform, worldTransform.currentTransform, alpha);

        tmp.set(interpWorld).mul(renderC.localFromEntity);
        renderC.renderPart.transform.set(tmp);
    }

    private void interpolateTRS(Matrix4 out, Matrix4 prev, Matrix4 curr, float alpha) {
        prev.getTranslation(p0);
        prev.getRotation(r0, true);
        prev.getScale(s0);

        curr.getTranslation(p1);
        curr.getRotation(r1, true);
        curr.getScale(s1);

        p.set(p0).lerp(p1, alpha);
        s.set(s0).lerp(s1, alpha);
        r.set(r0).slerp(r1, alpha);

        // compose: T * R * S
        out.idt().translate(p).rotate(r).scale(s.x, s.y, s.z);
    }

}
