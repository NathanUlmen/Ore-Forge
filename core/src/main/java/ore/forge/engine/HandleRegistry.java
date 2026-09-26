package ore.forge.engine;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntArray;
import ore.forge.engine.resources.ResourceSlot;
import ore.forge.engine.resources.ResourceSlot.LoadState;


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
        if (!target.isValid()) {
            throw new IllegalStateException("Handle is not valid");
        }
        int count = handleLookup.get(target.index()).give();
//        Gdx.app.log(LOG_TAG, "Acquiring Handle with count=" + count);
        return new Handle<>(target);
    }

    /**
     *
     * @return if the value has been removed from the registry.
     *
     */
    public boolean releaseHandle(Handle<E> handle) {
        if (handle == null || !handle.isValid()) {
            return false;
        }
        var entry = handleLookup.get(handle.index());
        if (entry.version == handle.version()) {
            int count = entry.takeBack();
            Gdx.app.log(LOG_TAG, "released handle. count=" + count);
            if (count <= 0) {
                Gdx.app.log(LOG_TAG, "Freeing Resource");
                removeResource(handle);
                handle.invalidate();
                return true;
            }
        }
        return false;
    }

    public E getResource(Handle<E> handle) {
        if (handle == null) {
            throw new IllegalArgumentException("Handle must not be null.");
        }

        int index = handle.index();
        if (!handle.isValid() || index >= handleLookup.size) {
            throw new IllegalArgumentException("Handle is invalid or index is greater than table size.");
        }

        Entry<E> entry = handleLookup.get(index);
        if (entry == null ) {
            throw new IllegalStateException("Entry was null");
        }

        if (entry.version != handle.version()) {
           throw new IllegalStateException("Entry version missmatch. Expected:" + entry.version + ", got:" + handle.version());
        }

        return entry.data();
    }

    public Handle<E> addResource(E resourceData, LoadState state) {
        int index = handleLookup.size;
        int version = versionCounter++;
        if (!freeList.isEmpty()) {
            index = freeList.pop();
            handleLookup.set(index, new Entry<>(version, createSlot(resourceData, state), 1));
        } else {
            handleLookup.add(new Entry<>(version, createSlot(resourceData, state), 1));
        }

        return new Handle<E>(index, version);
    }

    private void removeResource(Handle<E> targetHandle) {
        int index = targetHandle.index();

        isValid(targetHandle);

        Entry<E> entry = handleLookup.get(index);
        if (entry == null || entry.version != targetHandle.version()) {
            Gdx.app.error(LOG_TAG, "Target handle " + targetHandle + " has invalid version. Expected version=" + entry.version, new IllegalArgumentException());
        }

        handleLookup.set(index, null);
        freeList.add(index);
    }

    public ResourceSlot<E> getResourceSlot(Handle<E> handle) {
        var entry = handleLookup.get(handle.index());
        return entry == null ? null : entry.slot();
    }

    public ResourceSlot<E> createSlot(E resourceData, LoadState state) {
        return new ResourceSlot<>(resourceData, state);
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

    public void isValid(Handle<E> handle) {
        if (!handle.isValid()) {
            Gdx.app.error(LOG_TAG, "Target handle " + handle + " has invalid index of 0.", new IllegalArgumentException());
        }
    }

    private static class Entry<E extends Disposable> {
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

        public int takeBack() {
            return --checkoutCount;
        }
    }

}
