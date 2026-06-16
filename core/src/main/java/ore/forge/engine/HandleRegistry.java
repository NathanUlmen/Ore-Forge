package ore.forge.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;


/**
 * @author Nathan Ulmen
 *
 */
public class HandleRegistry<E extends Disposable> {
    private final Array<Entry<E>> handleLookup = new Array<>(128);
    private final IntArray freeList = new IntArray(false, 32);
    private int versionCounter = 1;

    public E getResource(Handle<E> handle) {
        int index = handle.index();
        if (!handle.isValid() || index >= handleLookup.size) {
            assert false : "Handle is invalid or index is greater than table size.";
            return null;
        }

        Entry<E> entry = handleLookup.get(index);
        if (entry == null || entry.version != handle.version()) {
            assert false : "Entry was null or version missmatch";
            return null;
        }

        return entry.data;
    }

    public Handle<E> addResource(E resourceData) {
        int index = handleLookup.size;
        int version = versionCounter++;
        if (!freeList.isEmpty()) {
            index = freeList.pop();
            handleLookup.set(index, new Entry<>(version, resourceData));
        } else {
            handleLookup.add(new Entry<>(version, resourceData));
        }

        return new Handle<E>(index, version);
    }

    public void removeResource(Handle<E> targetHandle) {
        int index = targetHandle.index();
        if (!targetHandle.isValid()) {
            throw new IllegalStateException("");
        }

        Entry<E> entry = handleLookup.get(index);
        if (entry == null || entry.version != targetHandle.version()) {
            throw new IllegalStateException();
        }

        handleLookup.set(index, null);
        entry.data.dispose();
        freeList.add(index);
    }

    public int size() {
        int nonNull = 0;
        for (Entry<E> entry : handleLookup) {
            if (entry.data != null) {
                nonNull++;
            }
        }
        return nonNull;
    }

    public String toString() {
        String s = "";
        s += "{HandleRegistry: activeResources: " + size() + " freeListSize: " + freeList.size + "}";
        return s;
    }

    private record Entry<E>(int version, E data) {
    }
}
