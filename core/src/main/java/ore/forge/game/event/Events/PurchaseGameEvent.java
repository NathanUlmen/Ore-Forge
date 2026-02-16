package ore.forge.game.event.Events;

import ore.forge.game.CurrencyType;
import ore.forge.FontColors;
import ore.forge.game.items.ItemDefinition;

public record PurchaseGameEvent(ItemDefinition item, CurrencyType currencyType, int amountPurchased) implements GameEvent {

    @Override
    public Class getEventType() {
        return PurchaseGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Purchased " + amountPurchased + " " + item.name() + " for " + (item.getAcquisitionInfo().itemValue() * amountPurchased) + " " + currencyType;
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return "Purchase Event";
    }

    @Override
    public FontColors getColor() {
        return FontColors.SEA_GREEN;
    }
}
