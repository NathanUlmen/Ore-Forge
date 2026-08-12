package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import ore.forge.ComponentDefinition;
import ore.forge.engine.components.TransformC;

public class TransformDefinition implements ComponentDefinition<TransformC> {
    private final Vector3 localPosition;
    private final Quaternion localRotation;
    private final Vector3 localScale;

    public TransformDefinition(Vector3 localPosition, Quaternion localRotation, Vector3 localScale) {
        this.localPosition = new Vector3(localPosition);
        this.localRotation = new Quaternion(localRotation);
        this.localScale = new Vector3(localScale);
    }

    @Override
    public TransformC create() {
        TransformC component = new TransformC();
        component.setBothLocal(localPosition, localRotation, localScale);
        return component;
    }
}
