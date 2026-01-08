package ore.forge.Player;

import ore.forge.Items.ItemDefinition;

import java.util.HashMap;
import java.util.List;

public class ItemInventory {
    private final HashMap<String, ItemInventoryNode> nodes;

    public ItemInventory(List<ItemDefinition> definitions) {
        nodes = new HashMap<>();

        // Create all our nodes
        for (ItemDefinition definition : definitions) {
            var node = new ItemInventoryNode(definition, 99);
            nodes.put(node.id(), node);
        }

        // Set our total owned values for each node
        loadNodeData();

    }

    private void loadNodeData() {

    }

    public void saveNodeData() {

    }

    public ItemInventoryNode getNode(String targetId) {
        return nodes.get(targetId);
    }

}
