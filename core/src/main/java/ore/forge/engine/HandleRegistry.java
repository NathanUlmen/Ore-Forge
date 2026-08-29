package ore.forge.engine;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;

import ore.forge.engine.resources.ResourceSlot;


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

        return entry.data();
    }

    public Handle<E> addResource(E resourceData) {
        int index = handleLookup.size;
        int version = versionCounter++;
        if (!freeList.isEmpty()) {
            index = freeList.pop();
            handleLookup.set(index, new Entry<>(version, createSlot(resourceData), 1));
        } else {
            handleLookup.add(new Entry<>(version, createSlot(resourceData), 1));
        }

        return new Handle<E>(index, version);
    }

    public void removeResource(Handle<E> targetHandle) {
        int index = targetHandle.index();
        if (!targetHandle.isValid()) {
            Gdx.app.error(LOG_TAG, "Target handle " + targetHandle.toString() + " has invalid index of 0.", new IllegalArgumentException());
        }

        Entry<E> entry = handleLookup.get(index);
        if (entry == null || entry.version != targetHandle.version()) {
            Gdx.app.error(LOG_TAG, "Target handle " + targetHandle.toString() + " has invalid version. Expected version=" + entry.version, new IllegalArgumentException());
        }

        handleLookup.set(index, null);
        entry.slot().dispose();
        freeList.add(index);
    }
    
    public ResourceSlot<E> getResourceSlot(Handle<E> handle) {
        var entry = handleLookup.get(handle.index());
        return entry == null ? null : entry.slot();
    }

    public ResourceSlot<E> createSlot(E resourceData) {
        return new ResourceSlot<>(resourceData);
    }

    public int size() {
        int nonNull = 0;
        for (Entry<E> entry : handleLookup) {
            if (entry != null && entry.slot != null) {
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

    private class Entry<E extends Disposable> { 
        private int checkoutCount;
        private final ResourceSlot<E> slot;
        private final int version;

        public Entry(int version, ResourceSlot<E> slot, int checkoutCount) {
            this.version = version;
            this.slot = slot;
            this.checkoutCount = checkoutCount;
        }

        public int getCheckoutCount() {
            return checkoutCount;
        } 
        
        public int version() {
            return version;
        }
        
        ResourceSlot<E> slot() {
            return slot;
        }
        
        public E data() {
            return slot.getData();
        }
        
        public int give() {
            return ++checkoutCount;
        }

        public int take() {
            return --checkoutCount;
        }
    }

}
