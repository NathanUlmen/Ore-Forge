package ore.forge.game.event.Events;

import ore.forge.FontColors;
import ore.forge.game.player.ItemInventoryNode;

public record InventoryNodeGameEvent(ItemInventoryNode node) implements GameEvent<ItemInventoryNode> {
    @Override
    public Class<?> getEventType() {
        return InventoryNodeGameEvent.class;
    }

    @Override
    public ItemInventoryNode getSubject() {
        return node;
    }

    @Override
    public String getBriefInfo() {
        return node.getPlaced() + " updated.";
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return "Inventory Node Event";
    }

    @Override
    public FontColors getColor() {
        return FontColors.YELLOW;
    }
}
