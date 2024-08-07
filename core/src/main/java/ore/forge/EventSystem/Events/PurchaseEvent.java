package ore.forge.EventSystem.Events;

import ore.forge.Currency;
import ore.forge.FontColors;
import ore.forge.Items.Item;

public record PurchaseEvent(Item item, Currency currency, int amountPurchased) implements Event {

    @Override
    public Class getEventType() {
        return PurchaseEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Purchased " + amountPurchased + " " + item.getName() + " for " + (item.getItemValue() * amountPurchased) + " " + currency;
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
