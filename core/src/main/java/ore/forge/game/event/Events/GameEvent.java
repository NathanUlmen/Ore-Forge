package ore.forge.game.event.Events;

import ore.forge.FontColors;

public interface GameEvent<E> {
    Class<?> getEventType();

    E getSubject();

    String getBriefInfo();

    String getInDepthInfo();

    String eventName();

    FontColors getColor();
}
