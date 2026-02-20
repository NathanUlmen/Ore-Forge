package ore.forge.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector3;

public class DirectionC implements Component {
    final Vector3 offset = new Vector3(); //Offset from transform
    final Vector3 direction = new Vector3(); //computed/derived value.
}
