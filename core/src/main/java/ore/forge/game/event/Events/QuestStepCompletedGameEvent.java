package ore.forge.game.event.Events;

import ore.forge.FontColors;
import ore.forge.game.quest.QuestStep;

public record QuestStepCompletedGameEvent(QuestStep step) implements GameEvent {

    @Override
    public Class getEventType() {
        return QuestStepCompletedGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "Completed " + step.getStepName() + " quest step.";
    }

    @Override
    public String getInDepthInfo() {
        return "Unimplemented";
    }

    @Override
    public String eventName() {
        return "Quest Step Completed";
    }

    @Override
    public FontColors getColor() {
        return FontColors.LIGHT_GREEN;
    }
}
