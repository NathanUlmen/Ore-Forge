package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;

public class TeleportRequestC implements Component {
    public final Matrix4 targetRootWorld = new  Matrix4();
}
