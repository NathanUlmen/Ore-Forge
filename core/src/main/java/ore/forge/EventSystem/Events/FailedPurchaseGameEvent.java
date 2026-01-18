package ore.forge.EventSystem.Events;

import ore.forge.CurrencyType;
import ore.forge.FontColors;
import ore.forge.Items.ItemDefinition;

public record FailedPurchaseGameEvent(ItemDefinition item, CurrencyType currencyType, int amount) implements GameEvent {

    @Override
    public Class getEventType() {
        return FailedPurchaseGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Failed to purchase " + item.name() + ". Not enough " + currencyType;
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return "Purchase Failed";
    }

    @Override
    public FontColors getColor() {
        return FontColors.CRIMSON;
    }
}
