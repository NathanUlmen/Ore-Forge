package ore.forge.game.event.Events;

import ore.forge.FontColors;
import ore.forge.game.quest.Quest;

public record QuestCompletedGameEvent(Quest compeletedQuest) implements GameEvent<Quest> {

    @Override
    public Class getEventType() {
        return QuestCompletedGameEvent.class;
    }

    public Quest getSubject() {
        return compeletedQuest;
    }

    @Override
    public String getBriefInfo() {
        return "Unimplemented";
    }

    @Override
    public String getInDepthInfo() {
        return "Unimplemented";
    }

    @Override
    public String eventName() {
        return "Quest Completed";
    }

    @Override
    public FontColors getColor() {
        return FontColors.FOREST_GREEN;
    }
}
