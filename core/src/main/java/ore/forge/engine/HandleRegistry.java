package ore.forge.engine;

import java.util.concurrent.CompletableFuture;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;


/**
 * @author Nathan Ulmen
 * Handle Registry is responsible for handing out Handles to resources and performing reference counting of 
 * resources stored inside it.
 *
 */
public class HandleRegistry<E extends Disposable> {
    private static final String LOG_TAG = HandleRegistry.class.getName();
    private final Array<Entry<E>> handleLookup = new Array<>(128);
    private final IntArray freeList = new IntArray(false, 32);
    private int versionCounter = 1;

    public HandleRegistry() {

    }

    public Handle<E> accquireHandle(Handle<E> target) {
        int count = handleLookup.get(target.index()).give();
        Gdx.app.log(LOG_TAG,"Accquiring Handle with count=" + count);
        return target;
    }

    public void releaseHandle(Handle<E> handle) {
        if (handle == null) {return;}
        int count = handleLookup.get(handle.index()).take();
        Gdx.app.log(LOG_TAG,"released handle. count=" + count);
        if (count <= 0) {
            Gdx.app.log(LOG_TAG,"Freeing Resource");
            removeResource(handle);
        }
    }

    public E getResource(Handle<E> handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Handle must not be null.");
        }
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
            handleLookup.set(index, new Entry<>(version, resourceData, 1));
        } else {
            handleLookup.add(new Entry<>(version, resourceData, 1));
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
            if (entry != null && entry.data != null) {
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

    private class Entry<E> { 
        private int checkoutCount;
        private final E data;
        private final int version;

        public Entry(int version, E data, int checkoutCount) {
            this.version = version;
            this.data = data;
            this.checkoutCount = checkoutCount;
        }

        public int getCheckoutCount() {
            return checkoutCount;
        } 
        
        public int version() {
            return version;
        }
        
        public E data() {
            return data;
        }
        
        public int give() {
            return ++checkoutCount;
        }

        public int take() {
            return --checkoutCount;
        }
    }

}
