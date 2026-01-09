package ore.forge;

import ore.forge.Strategies.Updatable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TimerUpdater {
    private static final List<Updatable> updatables = new ArrayList<>();
    private static final Deque<Updatable> removalQueue = new ArrayDeque<>();
    private static final Deque<Updatable> insertionQueue = new ArrayDeque<>();

    public static void update(float deltaTime) {
        for (Updatable updatable : updatables) {
            updatable.update(deltaTime, null);
        }
        //remove expired Updatables from list and add new ones
        while (!removalQueue.isEmpty()) {
            updatables.remove(removalQueue.pop());
        }
        while (!insertionQueue.isEmpty()) {
            updatables.add(insertionQueue.pop());
        }
    }

    public static void register(Updatable updatable) {
        insertionQueue.add(updatable);
    }

    public static void unregister(Updatable updatable) {
        removalQueue.add(updatable);
    }

    public static int count() {
        return updatables.size();
    }

}
