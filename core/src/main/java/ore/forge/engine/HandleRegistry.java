package ore.forge.engine;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import ore.forge.engine.definitions.Asset;

import java.util.ArrayList;

public class HandleRegistry<E extends Disposable> {
    private final Array<Entry<E>> handleLookup = new Array<>(false, 128);
    private final IntArray freeList = new IntArray(false,32);
    private int versionCounter = 0;

    public E getResource(Handle<E> handle) {
        int index = handle.index();
        if (!handle.isValid() || index >= handleLookup.size) {return null;}

        Entry<E> entry =  handleLookup.get(index);
        if (entry == null || entry.version != handle.version()) { return null; }

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
            //throw exception.
        }

        Entry<E> entry = handleLookup.get(index);
        if(entry == null || entry.version != targetHandle.version()) {
            throw new IllegalStateException();
        }

        handleLookup.set(index, null);
        entry.data.dispose();
        freeList.add(index);
    }

    private record Entry<E>(int version, E data) {}

}
