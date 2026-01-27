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

    public StagedCollection(int initialCapacity) {
        elements = new Array<>(false, initialCapacity);
        toAdd = new ObjectSet<>(initialCapacity / 2);
        toRemove = new ObjectSet<>(initialCapacity / 2);
    }

    public void flush() {
        if (toRemove.size > 0) {
            for (E e : toRemove) {
                elements.removeValue(e, true);
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
        assert !toRemove.contains(element);
        if (element == null) { throw new NullPointerException("Element cannot be null."); }
        if (toRemove.contains(element)) { throw new IllegalStateException("Element already exists." + element); }
        toAdd.add(element);
    }

    @NonNull
    public void stageRemoval(E element) {
        assert !toAdd.contains(element);
        if (element == null) { throw new NullPointerException("Element cannot be null."); }
        if (toAdd.contains(element)) { throw new IllegalStateException("Element already exists." + element); }
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
        return "Size: " + size() + "\tToAdd: " + toAdd.size + "\tToRemove: " + toRemove.size;
    }

}

