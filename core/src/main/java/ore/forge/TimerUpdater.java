package ore.forge;

import ore.forge.Strategies.TimeUpdatable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TimerUpdater {
    private static final List<TimeUpdatable> updatables = new ArrayList<>();
    private static final Deque<TimeUpdatable> removalQueue = new ArrayDeque<>();
    private static final Deque<TimeUpdatable> insertionQueue = new ArrayDeque<>();

    public static void update(float deltaTime) {
        for (TimeUpdatable updatable : updatables) {
            updatable.update(deltaTime);
        }
        //remove expired Updatables from list and add new ones
        while (!removalQueue.isEmpty()) {
            updatables.remove(removalQueue.pop());
        }
        while (!insertionQueue.isEmpty()) {
            updatables.add(insertionQueue.pop());
        }
    }

    public static void register(TimeUpdatable updatable) {
        insertionQueue.add(updatable);
    }

    public static void unregister(TimeUpdatable updatable) {
        removalQueue.add(updatable);
    }

    public static int count() {
        return updatables.size();
    }

}
