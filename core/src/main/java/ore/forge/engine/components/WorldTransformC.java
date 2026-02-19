package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Nathan Ulmen
 * This is the DERIVED transform, not authoritative.
 *
 */
public class WorldTransformC implements Component {
    public final Matrix4 currentTransform = new Matrix4().idt();
    public final Matrix4 previousTransform = new Matrix4().idt();

    public void advance() {
        previousTransform.set(currentTransform);
    }

    public void setBoth(Matrix4 newMat) {
        currentTransform.set(newMat);
        previousTransform.set(newMat);
    }

}
