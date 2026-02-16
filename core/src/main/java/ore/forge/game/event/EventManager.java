package ore.forge.game.event;


import ore.forge.game.event.Events.GameEvent;
import ore.forge.game.screens.EventLogger;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * @author Nathan Ulmen
 */
public class EventManager {
    private static EventManager eventManager = new EventManager();
    private final HashMap<Class<?>, ArrayList<GameEventListener<?>>> subscribers;
    private EventLogger eventLogger;

    public EventManager() {
        subscribers = new HashMap<>();
    }

    public static EventManager getSingleton() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }

    public void registerListener(GameEventListener<?> listener) {
        addListener(listener);
    }

    private void addListener(GameEventListener<?> gameEventListener) {
        var eventType = gameEventListener.getEventType();
        assert GameEvent.class.isAssignableFrom(eventType);
        if (!subscribers.containsKey(eventType)) {
            subscribers.put(eventType, new ArrayList<>());
            subscribers.get(eventType).add(gameEventListener);
        } else {
            subscribers.get(eventType).add(gameEventListener);
        }
    }

    public void unregisterListener(GameEventListener<?> listener) {
        removeListener(listener);
    }

    private void removeListener(GameEventListener<?> listener) {
        subscribers.get(listener.getEventType()).remove(listener);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void notifyListeners(GameEvent<?> event) {

        if (eventLogger != null) {
            this.eventLogger.logEvent(event);
        }

        ArrayList<GameEventListener<?>> listeners;
        if (subscribers.get(event.getEventType()) != null) {
            listeners = new ArrayList<>(subscribers.get(event.getEventType()));
            if (!listeners.isEmpty()) {
                for (GameEventListener listener : listeners) {
                    listener.handle(event);
                }
            }
        }
    }

    public void setEventLogger(EventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    public boolean hasListener(GameEventListener<?> listener) {
        return subscribers.get(listener.getEventType()).contains(listener);
    }

    public String toString() {
        return subscribers.toString();
    }

}
