package ore.forge.EventSystem.Events;

import ore.forge.FontColors;
import ore.forge.Items.ItemDefinition;

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
