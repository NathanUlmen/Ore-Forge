package ore.forge.game.event.Events;


import ore.forge.FontColors;
import ore.forge.game.items.ItemDefinition;
import ore.forge.game.Ore;

public record OreDroppedGameEvent(Ore ore, ItemDefinition dropper) implements GameEvent<Ore> {

    @Override
    public Class<?> getEventType() {
        return OreDroppedGameEvent.class;
    }

    @Override
    public Ore getSubject() {
        return ore;
    }

    @Override
    public String getBriefInfo() {
        return ore.getName() + " dropped by " + dropper.name();
    }

    @Override
    public String getInDepthInfo() {
        return "Unimplemented";
    }

    @Override
    public String eventName() {
        return "Ore Dropped Event";
    }

    @Override
    public FontColors getColor() {
        return FontColors.TURQUOISE;
    }

}
