package ore.forge.engine.components;

import com.badlogic.gdx.math.Matrix4;
import ore.forge.engine.TransformHistory;
import ore.forge.engine.render.RenderPart;

public class RenderComponent {
    public RenderPart renderPart;
    public Matrix4 localFromRoot; //Offset from root.
    public int drivenByBody; //optional: index to body that part is driven by.
    public TransformHistory localFromBody; //optional: if driven by body.
}
