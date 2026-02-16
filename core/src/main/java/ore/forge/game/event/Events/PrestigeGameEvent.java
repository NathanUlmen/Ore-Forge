package ore.forge.game.event.Events;

import ore.forge.FontColors;

public record PrestigeGameEvent(Boolean result) implements GameEvent<Boolean> {

    @Override
    public Class<?> getEventType() {
        return PrestigeGameEvent.class;
    }

    @Override
    public Boolean getSubject() {
        return result;
    }

    @Override
    public String getBriefInfo() {
        throw new UnsupportedOperationException("Not supported yet.");
//        if (result) {
//            return "Successfully prestiged to level " + (ore.forge.Player.Player.getSingleton().getPrestigeLevel() + 1);
//        }
//        return "Failed to prestige.";
    }

    @Override
    public String getInDepthInfo() {
        return "";
    }

    @Override
    public String eventName() {
        return result ? "Successful Prestige" : "Failed Prestige";
    }

    @Override
    public FontColors getColor() {
        return result ? FontColors.SKY_BLUE : FontColors.RED;
    }
}
