package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;

public class ChildC implements Component {
    public Entity parent; //reference to parent entity.

    //If child inherits parent transform every tick.
    public boolean inheritTransform = true;
}
