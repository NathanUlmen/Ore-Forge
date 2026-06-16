package ore.forge.game;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import ore.forge.ComponentDefinition;

/**
 * @author Nathan Ulmen
 *
 *
 */
public class EntityDefinition {
    private Array<ComponentDefinition<?>> components;


    public Component[] createInstance() {
        Component[] builtComponents = new Component[components.size];

        for (int i = 0; i < components.size; i++) {
            builtComponents[i] = components.get(i).create();
        }

        return builtComponents;
    }

}
