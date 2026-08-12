package ore.forge.game.temp;

import ore.forge.ComponentDefinition;

public class ItemComponentDefinition implements ComponentDefinition<ItemComponent> {
    @Override
    public ItemComponent create() {
        return new ItemComponent();
    }
}
