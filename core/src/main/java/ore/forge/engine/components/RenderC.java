package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.render.RenderPart;


/**
 * @author Nathan Ulmen
 * A {@link RenderC} is ...
 * It holds a reference to a RenderPart
 * It also
 * */
public class RenderC implements Component {
    public RenderPart renderPart;

    //Required
    public final Vector3 scale = new Vector3(1,1,1);

    //Constant offset from entity Transform to mesh, if needed/optional
    public final Matrix4 localFromEntity = new Matrix4().idt();

}
