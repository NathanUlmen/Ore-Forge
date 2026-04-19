package ore.forge.game.event.Events;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector3;
import ore.forge.FontColors;
import ore.forge.engine.components.TransformC;
import ore.forge.game.items.ItemDefinition;

public record ItemPlacedGameEvent(ItemDefinition item, Entity instance) implements GameEvent {

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
        Vector3 translation;
        translation = instance.getComponent(TransformC.class).localPosition;
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
