package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public record NodeInfo(String behaviorKey,
                String collisionType,
                Vector3 relativeDirection,
                Matrix4 transform) {
}
