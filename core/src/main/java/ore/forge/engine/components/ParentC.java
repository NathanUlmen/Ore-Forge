package ore.forge.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;


public class ParentC implements Component {
    public final Array<Entity> children = new Array<>(false, 6);
    public boolean destroyChildrenWithParent = true;

    public void add(Entity entity) {
        children.add(entity);
    }

}
