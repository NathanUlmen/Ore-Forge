package ore.forge;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.mongodb.lang.NonNull;

import java.util.*;

public class StagedCollection<E> implements Iterable<E> {
    private final Array<E> elements;
    private final ObjectSet<E> toAdd, toRemove;

    public StagedCollection() {
        this(1_000);
    }

    public StagedCollection(int startSize) {
        elements = new Array<>(false, startSize);
        toAdd = new ObjectSet<>(startSize / 2);
        toRemove = new ObjectSet<>(startSize / 2);

    }

    public void flush() {
        if (toRemove.size > 0) {
            for (int i = elements.size - 1; i >= 0; i--) {
                if (toRemove.contains(elements.get(i))) {
                    elements.removeIndex(i);
                }
            }
            toRemove.clear();
        }

        if (toAdd.size > 0) {
            for (E e : toAdd) {
                elements.add(e);
            }
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
        return elements.size;
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
        return "Size: " + size() +  "\tToAdd: " + toAdd.size  + "\tToRemove: " + toRemove.size;
    }

}

