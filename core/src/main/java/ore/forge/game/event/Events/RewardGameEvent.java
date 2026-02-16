package ore.forge.game.event.Events;

import ore.forge.FontColors;
import ore.forge.game.items.ItemDefinition;

public record RewardGameEvent(ItemDefinition reward, int count) implements GameEvent {

    @Override
    public Class getEventType() {
        return RewardGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Unimplemented REWARD Event";
    }

    @Override
    public String getInDepthInfo() {
        return "Unimplemented Reward Event";
    }

    @Override
    public String eventName() {
        return "Reward";
    }

    @Override
    public FontColors getColor() {
        return null;
    }
}
