package ore.forge.EventSystem.Events;

import ore.forge.Currency;
import ore.forge.FontColors;
import ore.forge.Items.Item;

public record FailedPurchaseEvent(Item item, Currency currency, int amount) implements Event{

    @Override
    public Class getEventType() {
        return FailedPurchaseEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Failed to purchase " + item.getName() + ". Not enough " + currency;
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
