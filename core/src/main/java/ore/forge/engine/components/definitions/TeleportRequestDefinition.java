package ore.forge.engine.components.definitions;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.ComponentDefinition;
import ore.forge.engine.components.TeleportRequestC;

public class TeleportRequestDefinition implements ComponentDefinition<TeleportRequestC> {
    private final Matrix4 targetRootWorld;

    public TeleportRequestDefinition(Matrix4 targetRootWorld) {
        this.targetRootWorld = new Matrix4(targetRootWorld);
    }

    @Override
    public TeleportRequestC create() {
        TeleportRequestC component = new TeleportRequestC();
        component.targetRootWorld.set(targetRootWorld);
        return component;
    }
}
