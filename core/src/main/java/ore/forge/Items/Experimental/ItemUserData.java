package ore.forge.Items.Experimental;

import com.badlogic.gdx.math.Vector3;
import ore.forge.Strategies.Behavior;

public record ItemUserData(Vector3 direction, Behavior behavior, ItemBlueprint blueprint) {}
