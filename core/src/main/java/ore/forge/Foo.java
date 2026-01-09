package ore.forge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Foo<E> {
    private final List<E> elements;
    private final Deque<E> toAdd, toRemove;

    public Foo() {
        elements = new ArrayList<>();
        toAdd = new ArrayDeque<>();
        toRemove = new ArrayDeque<>();
    }

    public List<E> getElements() {
        return elements;
    }

    public void updateLists() {
        while (!toRemove.isEmpty()) {
            elements.remove(toRemove.pop());
        }

        while (!toAdd.isEmpty()) {
            elements.add(toAdd.pop());
        }
    }

    public void add(E element) {
        toAdd.push(element);
    }

    public void remove(E element) {
        toRemove.push(element);
    }

}
