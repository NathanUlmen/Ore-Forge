package ore.forge.engine.components.definitions;

import ore.forge.ComponentDefinition;
import ore.forge.engine.components.ParentC;

public class ParentDefinition implements ComponentDefinition<ParentC> {
    private final boolean destroyChildrenWithParent;

    public ParentDefinition(boolean destroyChildrenWithParent) {
        this.destroyChildrenWithParent = destroyChildrenWithParent;
    }

    @Override
    public ParentC create() {
        ParentC component = new ParentC();
        component.destroyChildrenWithParent = destroyChildrenWithParent;
        return component;
    }
}
