package ore.forge.engine.definitions;

import com.badlogic.ashley.core.Component;
import ore.forge.engine.serialization.ComponentLoader;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathan Ulmen
 * A definition tree is an itermidiate form that all {@link Definition} implementation
 * have. Each {@link Definition} will have an {@link DefinitionBuilder} associated with it.
 * These builders are responsible for interpreting/transforming a {@link DefinitionTree}
 * into the final {@link Definition} which is just raw data.
 * <p>
 * - Nathan Ulmen, April 1, 2026
 *
 */
public class DefinitionTree {
    private final ArrayList<ComponentNode> nodes;

    public DefinitionTree() {
        nodes = new ArrayList<>();
    }

    public Component[]  getComponents() {
        Component[] components = new Component[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            components[i] = nodes.get(i).data();
        }
        return components;
    }

    public static class ComponentNode {
        private final Component data;
        private ComponentNode parent;
        private List<ComponentNode> children;

        public ComponentNode(Component data) {
            this.data = data;
            children = new ArrayList<>();
        }

        public Component data() {
            return data;
        }

        public ComponentNode parent() {
            return parent;
        }

        public List<ComponentNode> children() {
            return children;
        }

    }

}
