package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class DirectionC implements Component {
    public final Vector3 directionOffset = new Vector3(); //Offset from transform
    public transient final Vector3 direction = new Vector3(); //computed/derived value.
}
