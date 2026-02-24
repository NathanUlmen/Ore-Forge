package ore.forge.game.collisions;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public final class CollisionHandlerC implements Component {

    public final IntMap<HandlerSet> perChild = new IntMap<>();

    public HandlerSet handlersForChildIndex(int childIndex) {
        return perChild.get(childIndex);
    }

    public static final class HandlerSet {
        public final Array<OnContactStart> starts = new Array<>();
        public final Array<OnContactTouching> touchings = new Array<>();
        public final Array<OnContactEnd> ended = new Array<>();
    }
}

