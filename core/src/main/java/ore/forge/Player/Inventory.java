package ore.forge.Player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import ore.forge.Constants;
import ore.forge.Items.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

//@author Nathan Ulmen
public class Inventory {

    private final ArrayList<InventoryNode> inventoryNodes;

    private final HashMap<String, Item> allItems;

    public Inventory(HashMap<String,Item> allGameItems, ArrayList<InventoryNode> savedInventory) {
        allItems = allGameItems;
        inventoryNodes = savedInventory;
        sortByType();
    }

//    public Inventory(String fileToParse, HashMap<String, Item> allItems) {
//        inventoryNodes = new ArrayList<>();
//        this.allItems = allItems;
//        JsonReader jsonReader = new JsonReader();
//        JsonValue fileContents = jsonReader.parse(Gdx.files.local(fileToParse));
//        if (fileContents != null) {
//            for (JsonValue jsonValue: fileContents) {
//            }
//        }
//
//    }

    public ArrayList<InventoryNode> getInventoryNodes() {
        return inventoryNodes;
    }


    public void printInventory() {
        for (InventoryNode node : inventoryNodes) {
            System.out.println("Name: " + node.getHeldItem().getName() + "\tNumber Owned: " + node.getTotalOwned());
        }
    }

    public void saveInventory() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        sortByName();

        List<InventoryData> inventoryDataList = new ArrayList<>();

        for (InventoryNode node : inventoryNodes) {
            InventoryData data = new InventoryData();
            data.setItemName(node.getHeldItem().getName());
            data.setType(node.getHeldItem().getClass().getSimpleName());
            data.setTotalOwned(node.getTotalOwned());
            inventoryDataList.add(data);
        }


        String jsonOutput = json.prettyPrint(inventoryDataList);
        FileHandle fileHandle = Gdx.files.local(Constants.INVENTORY_FP);
        fileHandle.writeString(jsonOutput, false);
//        System.out.println(jsonOutput);
    }

    public void loadInventory() {

    }

    public void sortByName() {
        NameComparator nameComparator = new NameComparator();
        bubbleSort(nameComparator);
    }

    public void sortByType() {
        TypeComparator typeComparator = new TypeComparator();
        bubbleSort(typeComparator);
    }

    public void sortByTier() {
        TierComparator tierComparator = new TierComparator();
        bubbleSort(tierComparator);
    }

    public void sortByStored() {
        StoredComparator storedComparator = new StoredComparator();
        bubbleSort(storedComparator);
    }

    private <E extends Comparator<InventoryNode>> void bubbleSort(E compareType) {
        for (int waterLine = inventoryNodes.size()-1; waterLine>=0; waterLine--) {
            for (int net = 0; net < waterLine; net++) {
                if (compareType.compare(inventoryNodes.get(net), inventoryNodes.get(net+1)) >0) {
                    InventoryNode temp = inventoryNodes.get(net);
                    inventoryNodes.set(net, inventoryNodes.get(net+1));
                    inventoryNodes.set(net+1, temp);
                }
            }
        }
    }

    //Make it not case-sensitive. E == e
    //Make it so it looks for items that contain string.
    // EX: Upgrade would return All items that have upgrade in name.
    public ArrayList<InventoryNode> searchFor(String userInput) {
        ArrayList<InventoryNode> desiredItems = new ArrayList<>();
        userInput = userInput.toLowerCase();
        for (InventoryNode node : inventoryNodes) {
            if (node.getName().toLowerCase().contains(userInput)) {
                desiredItems.add(node);
            }
        }

        return desiredItems;
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        for (InventoryNode node: inventoryNodes) {
            string.append(node.toString()).append("\n");
        }
        return string.toString();
    }

    static class NameComparator implements Comparator<InventoryNode> {
        @Override
        public int compare(InventoryNode node1, InventoryNode node2) {
            return node1.getName().compareTo(node2.getName());
        }
    }

    static class TierComparator implements Comparator<InventoryNode> {
        //Sort by Tier should sort by tier, then type, then Name.
        @Override
        public int compare(InventoryNode node1, InventoryNode node2) {
            //Tier
            int result = node1.getHeldItem().getTier().compareTo(node2.getHeldItem().getTier());
            if(result != 0) {return result;}
            //Type
            result = node1.getHeldItem().getClass().getSimpleName().
                    compareTo(node2.getHeldItem().getClass().getSimpleName());
            if (result != 0) {return result;}
            //Name
            return node1.getName().compareTo(node2.getName());

        }
    }

    static class TypeComparator implements Comparator<InventoryNode> {
        //Sort by type should sort by type, tier,  then name.
        @Override
        public int compare(InventoryNode node1, InventoryNode node2) {
            //Type
            int result = node1.getHeldItem().getClass().getSimpleName().
                                compareTo(node2.getHeldItem().getClass().getSimpleName());
            if (result != 0) {return result;}
            //Tier
            result = node1.getHeldItem().getTier().compareTo(node2.getHeldItem().getTier());
            if(result != 0) {return result;}
            //Name
            return node1.getName().compareTo(node2.getName());

        }
    }

    //Sort by Most to least, currently doesn't do that.
    static class StoredComparator implements Comparator<InventoryNode> {
        @Override
        public int compare(InventoryNode node1, InventoryNode node2) {
            Integer firstStored = node1.getStored();
            Integer secondStored = node2.getStored();
            return firstStored.compareTo(secondStored);
        }
    }

    private class InventoryData {
        private String itemName;
        private String type;
        private int totalOwned;

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTotalOwned() {
            return totalOwned;
        }

        public void setTotalOwned(int totalOwned) {
            this.totalOwned = totalOwned;
        }
    }

}
