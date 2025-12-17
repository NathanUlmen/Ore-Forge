package ore.forge.Player;


import ore.forge.EventSystem.EventManager;
import ore.forge.EventSystem.Events.InventoryNodeGameEvent;
import ore.forge.Items.*;

public class InventoryNode {
    private final String nodeName;
    private int totalOwned;//Accounts for the number placed on base and the number in the inventory. Decremented when sold or incremented when a new item is obtained.
    private int stored;//number In inventory currently, decremented when item is placed.
    private int placed;//number placed on base currently, increment when item is placed.
    private final Item heldItem;

    public InventoryNode(Item itemToBeHeld, int totalOwned) {
        heldItem = itemToBeHeld;
        this.totalOwned = totalOwned;
        stored = totalOwned;
        placed = 0;
        nodeName = itemToBeHeld.getName();
    }

    public Item createNewHeldItem() {
        assertCheck();
        return switch (heldItem) {
            case Upgrader upgrader -> new Upgrader(upgrader);
            case Furnace furnace -> new Furnace(furnace);
            case Dropper dropper -> new Dropper(dropper);
            case Conveyor conveyor -> new Conveyor(conveyor);
            default -> throw new IllegalStateException("Unexpected value: " + heldItem);
        };
    }

    public String getName() {
        return nodeName;
    }

    public int getTotalOwned() {
        return totalOwned;
    }

    public Item getHeldItem() {
        return heldItem;
    }

    public int getStored() {
        return stored;
    }

    public String getHeldItemID() {
        return heldItem.getID();
    }

    public void resetOwned() {
        stored = placed = totalOwned = 0;
    }

    public void place() {
        stored--;
        placed++;
        EventManager.getSingleton().notifyListeners(new InventoryNodeGameEvent(this));
    }

    public void pickUp() {
        stored++;
        placed--;
        EventManager.getSingleton().notifyListeners(new InventoryNodeGameEvent(this));
    }

    public boolean hasSupply() {
        assertCheck();
        return stored > 0;
    }

    public void addFromBase() {
        stored++;
        placed--;
        assertCheck();
    }

    public void addNew() {
        totalOwned++;
        stored++;
        EventManager.getSingleton().notifyListeners(new InventoryNodeGameEvent(this));
        assertCheck();
    }

    public void addNew(int numBought) {
        totalOwned += numBought;
        stored += numBought;
        EventManager.getSingleton().notifyListeners(new InventoryNodeGameEvent(this));
        assertCheck();
    }

    public void sellFromInventory() {
        totalOwned--;
        stored--;
        assertCheck();
    }

    public void sellFromInventory(int numSold) {
        totalOwned -= numSold;
        stored -= numSold;
        assertCheck();
    }

    public void sellFromBase() {
        totalOwned--;
        placed--;
        EventManager.getSingleton().notifyListeners(new InventoryNodeGameEvent(this));
        assertCheck();
    }

    public int getPlaced() {
        return placed;
    }

    private void assertCheck() {
        assert totalOwned == stored + placed;
    }

    public String toString() {
        return heldItem.getName() + "\tTotal Owned:" + totalOwned + "\tIn Inventory:" + stored + "\tPlaced:" + placed;
    }

}
