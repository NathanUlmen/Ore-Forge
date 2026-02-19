package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class TransformC implements Component {
    // Canonical LOCAL pose (relative to parent if ChildC.inheritTransform == true)
    public final Vector3 localPosition = new Vector3();
    public final Quaternion localRotation = new Quaternion(); // identity by default
    public final Vector3 localScale = new Vector3(1, 1, 1);

    // Previous LOCAL pose (for interpolation / history)
    public final Vector3 prevLocalPosition = new Vector3();
    public final Quaternion prevLocalRotation = new Quaternion();
    public final Vector3 prevLocalScale = new Vector3(1, 1, 1);

    public void advance() {
        prevLocalPosition.set(localPosition);
        prevLocalRotation.set(localRotation);
        prevLocalScale.set(localScale);
    }

    public void setBothLocal(Vector3 pos, Quaternion rot, Vector3 scl) {
        localPosition.set(pos);
        localRotation.set(rot);
        localScale.set(scl);

        prevLocalPosition.set(pos);
        prevLocalRotation.set(rot);
        prevLocalScale.set(scl);
    }

    /** Convenience: interpret matrix as LOCAL TRS. */
    public void setBothLocal(Matrix4 localMatrix) {
        localMatrix.getTranslation(localPosition);
        localMatrix.getRotation(localRotation);
        localMatrix.getScale(localScale);

        prevLocalPosition.set(localPosition);
        prevLocalRotation.set(localRotation);
        prevLocalScale.set(localScale);
    }

    /** Build a LOCAL matrix (useful for composition). */
    public Matrix4 toLocalMatrix(Matrix4 out) {
        return out.idt().translate(localPosition).rotate(localRotation).scale(
            localScale.x, localScale.y, localScale.z
        );
    }
}

