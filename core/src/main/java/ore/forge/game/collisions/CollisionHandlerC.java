package ore.forge.game.collisions;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public final class CollisionHandlerC implements Component {
    public final HandlerSet defaults = new HandlerSet();

    public final IntMap<HandlerSet> perChild = new IntMap<>();

    public HandlerSet handlersForChildIndex(int childIndex) {
        if (childIndex < 0) return defaults;
        HandlerSet set = perChild.get(childIndex);
        return (set != null) ? set : defaults;
    }

    public static final class HandlerSet {
        public final Array<OnContactStart> starts = new Array<>();
        public final Array<OnContactTouching> touchings = new Array<>();
        public final Array<OnContactEnd> ended = new Array<>();
    }
}

