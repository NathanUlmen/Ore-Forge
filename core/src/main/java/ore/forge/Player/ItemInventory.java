package ore.forge.Player;

import ore.forge.Items.Experimental.ItemDefinition;
import ore.forge.Items.Experimental.ItemSpawner;

import java.util.HashMap;
import java.util.List;

public class ItemInventory {
    private final HashMap<String, ItemInventoryNode> nodes;

    public ItemInventory(List<ItemDefinition> spawners) {
        nodes = new HashMap<>();

        // Create all our nodes
        for (ItemDefinition spawner : spawners) {
            var node = new ItemInventoryNode(spawner, 99);
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
