package ore.forge.Player;

import ore.forge.Items.ItemDefinition;

public class ItemInventoryNode {
    private boolean isUnlocked;
    private final ItemDefinition heldItem;
    private int totalOwned;
    private int stored;
    private int placed;

    public ItemInventoryNode(ItemDefinition toHold, int totalOwned) {
        this.heldItem = toHold;
        this.totalOwned = totalOwned;
        this.stored = totalOwned;
        this.placed = 0;
        this.isUnlocked = false;
    }

    public String name() {
        return heldItem.name();
    }

    public String id() {
        return heldItem.id();
    }

    public int getTotalOwned() {
        return totalOwned;
    }

    public int getStored() {
        return stored;
    }

    public int getPlaced() {
        return placed;
    }

    /**
     * Takes one item from inventory and places it.
     *
     * @return true if successful, false if no supply available
     */
    public boolean takeFrom() {
        if (stored <= 0) {
            return false;
        }

        stored--;
        placed++;
        return true;
    }

    /**
     * Returns a placed item back to inventory.
     *
     * @return true if successful, false if nothing was placed
     */
    public boolean giveBack() {
        if (placed <= 0) {
            return false;
        }

        placed--;
        stored++;
        return true;
    }

    /**
     * Used to increase the number owned of this item by the specified ammount.
     *
     * @param toAdd - the number of items to add to total owned.
     */
    public void addNew(int toAdd) {
        stored += toAdd;
        totalOwned += toAdd;
    }

    /**
     * @return true if at least one item is available in inventory
     */
    public boolean hasSupply() {
        return stored > 0;
    }

    @Override
    public String toString() {
        return String.format(
                "%s [stored=%d, placed=%d, total=%d]",
                name(), stored, placed, totalOwned);
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
