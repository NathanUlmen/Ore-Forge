package ore.forge;

import com.badlogic.gdx.utils.Array;
import com.mongodb.lang.NonNull;

import java.util.*;

public class StagedCollection<E> implements Iterable<E> {
    private final List<E> elements;
    private final Set<E> toAdd, toRemove;

    public StagedCollection() {
        elements = new ArrayList<>();
        toAdd = new HashSet<>();
        toRemove = new HashSet<>();
    }

    public StagedCollection(int startSize) {
        elements = new ArrayList<>(startSize);
        toAdd = new HashSet<>(startSize / 2);
        toRemove = new HashSet<>(startSize / 2);
    }

    public void flush() {
        if (!toRemove.isEmpty()) {
            if (toRemove.size() <= 40) {
                elements.removeAll(toRemove);
            } else {
                elements.removeIf(toRemove::contains);
            }
            toRemove.clear();
        }

        if (!toAdd.isEmpty()) {
            elements.addAll(toAdd);
            toAdd.clear();
        }
    }

    @NonNull
    public void stageAddition(E element) {
        if (element == null) {throw new NullPointerException("Element cannot be null.");}
        toRemove.remove(element);
        toAdd.add(element);
    }

    @NonNull
    public void stageRemoval(E element) {
        if (element == null) {throw new NullPointerException("Element cannot be null.");}
        toAdd.remove(element);
        toRemove.add(element);
    }

    public int size() {
        return elements.size();
    }

    public Iterable<E> toAdd() {
        return toAdd;
    }

    public Iterable<E> toRemove() {
        return toRemove;
    }

    @Override
    public Iterator<E> iterator() {
        return elements.iterator();
    }

    public String toString() {
        return "Size: " + size() +  "\tToAdd: " + toAdd.size()  + "\tToRemove: " + toRemove.size();
    }

}

