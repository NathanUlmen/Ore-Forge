package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;

/**
 * @author Nathan Ulmen
 * This is the derived transform
 *
 * TODO: implmenet dirty flag to prevent redundant/unessecary work.
 */
public class WorldTransformC implements Component {
    public final Matrix4 currentTransform = new Matrix4().idt();
    public final Matrix4 previousTransform = new Matrix4().idt();
//    public boolean dirty = false;

    public void advance() {
        previousTransform.set(currentTransform);
    }

    public void setBoth(Matrix4 newMat) {
        currentTransform.set(newMat);
        previousTransform.set(newMat);
    }

    public String toString() {
        return "WorldTransformC" + currentTransform;
    }
}
