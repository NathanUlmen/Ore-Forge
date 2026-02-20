package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.render.RenderPart;

public class RenderC implements Component {
    public RenderPart renderPart;

    //Constant offset from entity Transform to mesh, if needed/optional
    public final Matrix4 localFromEntity = new Matrix4().idt();
}
