package ore.forge.EventSystem.Events;

import ore.forge.FontColors;
import ore.forge.Items.ItemDefinition;

public record ItemRemovedGameEvent(ItemDefinition item) implements GameEvent<ItemDefinition> {
    @Override
    public Class<?> getEventType() {
        return ItemRemovedGameEvent.class;
    }

    @Override
    public ItemDefinition getSubject() {
        return item;
    }

    @Override
    public String getBriefInfo() {
        return "UNIMPLEMENTED";
    }

    @Override
    public String getInDepthInfo() {
        return "UNIMPLEMENTED";
    }

    @Override
    public String eventName() {
        return "Item Removed";
    }

    @Override
    public FontColors getColor() {
        return FontColors.DARK_RED;
    }
}
