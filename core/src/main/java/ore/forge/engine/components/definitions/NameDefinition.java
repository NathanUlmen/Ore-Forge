package ore.forge.engine.components.definitions;

import ore.forge.ComponentDefinition;
import ore.forge.engine.components.NameC;

public class NameDefinition implements ComponentDefinition<NameC> {
    private final String name;

    public NameDefinition(String name) {
        this.name = name;
    }

    @Override
    public NameC create() {
        NameC component = new NameC();
        component.name = name;
        return component;
    }
}
