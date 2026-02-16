package ore.forge.game.event.Events;


import ore.forge.FontColors;

public record OnUpgradeGameEvent() implements GameEvent {

    @Override
    public Class getEventType() {
        return OnUpgradeGameEvent.class;
    }

    @Override
    public Object getSubject() {
        return null;
    }

    @Override
    public String getBriefInfo() {
        return "";
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return "";
    }

    @Override
    public FontColors getColor() {
        return null;
    }
}
