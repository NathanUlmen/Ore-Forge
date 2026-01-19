package ore.forge.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.*;
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
        load();

    }


    public void load() {
        JsonReader reader = new JsonReader();
        JsonValue json = reader.parse(Gdx.files.local("itemSaveData.json"));
        if (json == null) {
            configDefault();
            return;
        }
        for (JsonValue value : json) {
            final String id = value.getString("id");
            final int totalOwned = value.getInt("totalOwned");
            final boolean isUnlocked = value.getBoolean("isUnlocked");

            ItemInventoryNode inventoryNode = nodes.get(id);
            if (inventoryNode == null) { throw new IllegalArgumentException("Cant find item with id of " + value.getString("id")); }
            inventoryNode.setIsUnlocked(isUnlocked);
            inventoryNode.setTotalOwned(totalOwned);
        }
    }


    public void saveNodeData() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);

        FileHandle file = Gdx.files.local("itemSaveData.json");

        // Convert collection to something serializable
        Array<ItemInventoryNode> nodeList = new Array<>();

        for (ItemInventoryNode node : nodes.values()) {
            nodeList.add(node);
        }

        file.writeString(json.prettyPrint(nodeList), false);
    }

    private void configDefault() {

    }

    public ItemInventoryNode getNode(String targetId) {
        return nodes.get(targetId);
    }

}
