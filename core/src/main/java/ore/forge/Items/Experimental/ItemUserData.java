package ore.forge.Items.Experimental;

import com.badlogic.gdx.physics.box2d.Body;
import ore.forge.Strategies.Behavior;

public record ItemUserData(float relativeAngle, Behavior behavior, Body body) {}
