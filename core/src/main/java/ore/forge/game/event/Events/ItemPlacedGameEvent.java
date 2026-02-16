package ore.forge.game.event.Events;

import com.badlogic.gdx.math.Vector3;
import ore.forge.engine.EntityInstance;
import ore.forge.FontColors;
import ore.forge.game.items.ItemDefinition;

public record ItemPlacedGameEvent(ItemDefinition item, EntityInstance instance) implements GameEvent {

    @Override
    public Class getEventType() {
        return ItemPlacedGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return ItemDefinition.class;
    }

    @Override
    public String getBriefInfo() {
        Vector3 translation = new Vector3();
        instance.transform().getTranslation(translation);
        return item.name() + " placed at " + translation;
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return "Item Placed";
    }

    @Override
    public FontColors getColor() {
        return FontColors.CHOCOLATE;
    }
}
