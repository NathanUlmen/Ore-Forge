package ore.forge.game.event.Events;


import ore.forge.FontColors;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.components.Ore;

public record OreSoldGameEvent(Ore ore, ItemDefinition item) implements GameEvent<Ore> {


    @Override
    public Class getEventType() {
        return OreSoldGameEvent.class;
    }

    @Override
    public Ore getSubject() {
        return ore;
    }

    @Override
    public String getBriefInfo() {
        return ore.getName() + " sold by " + item.name();
    }

    @Override
    public String getInDepthInfo() {
        var info = "";
        info += " Name: " + ore.getName() + " Value: " + ore.getOreValue() + " Temperature: " + ore.getOreTemp() + " Multiore: " + ore.getYield();
        info += "\nSold By: " + item.name();
        return info;
    }

    @Override
    public String eventName() {
        return "Ore Sold";
    }

    @Override
    public FontColors getColor() {
        return FontColors.DARK_SEA_GREEN;
    }
}
