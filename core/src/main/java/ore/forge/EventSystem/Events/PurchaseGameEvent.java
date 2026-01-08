package ore.forge.EventSystem.Events;

import ore.forge.Currency;
import ore.forge.FontColors;
import ore.forge.Items.ItemDefinition;

public record PurchaseGameEvent(ItemDefinition item, Currency currency, int amountPurchased) implements GameEvent {

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
        return "Purchased " + amountPurchased + " " + item.name() + " for " + (item.getAcquisitionInfo().itemValue() * amountPurchased) + " " + currency;
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
