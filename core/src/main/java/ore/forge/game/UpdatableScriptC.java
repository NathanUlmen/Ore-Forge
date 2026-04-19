package ore.forge.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

public class UpdatableScriptC implements Component {
    public final Array<Tickable> scripts = new Array<>(false, 8);

    public void add(Tickable script) {
        scripts.add(script);
    }
}
