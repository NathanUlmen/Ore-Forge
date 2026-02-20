package ore.forge.game.collisions;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

public final class CollisionHandlerC implements Component {
    public final Array<OnContactStart> starts = new Array<>();
    public final Array<OnContactTouching> touchings = new Array<>();
    public final Array<OnContactEnd> ended = new Array<>();
}

