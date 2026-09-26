package ore.forge.game;

import ore.forge.game.items.ItemDefinition;

import java.util.HashMap;
import java.util.Iterator;

public class ItemRegistry implements Iterable<ItemDefinition> {
    private final HashMap<String, ItemDefinition> itemDefinitions;

    private ItemRegistry() {
        this.itemDefinitions = new HashMap<>();
    }

    public void addDefinition(ItemDefinition def) {
        itemDefinitions.put(def.id(), def);
    }

    public ItemDefinition getDefinition(String id) {
        return itemDefinitions.get(id);
    }

    @Override
    public Iterator<ItemDefinition> iterator() {
        return itemDefinitions.values().iterator();
    }
}
