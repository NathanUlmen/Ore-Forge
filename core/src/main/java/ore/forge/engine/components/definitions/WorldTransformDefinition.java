package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.ComponentDefinition;
import ore.forge.engine.components.WorldTransformC;

public class WorldTransformDefinition implements ComponentDefinition<WorldTransformC> {
    private final Matrix4 transform;

    public WorldTransformDefinition(Matrix4 transform) {
        this.transform = new Matrix4(transform);
    }

    @Override
    public WorldTransformC create() {
        WorldTransformC component = new WorldTransformC();
        component.setBoth(transform);
        return component;
    }
}
