package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import ore.forge.Strategies.BodyLogic;

public record ItemUserData(Vector3 direction, BodyLogic bodyLogic, ItemSpawner blueprint, Matrix4 localTransform) {}
