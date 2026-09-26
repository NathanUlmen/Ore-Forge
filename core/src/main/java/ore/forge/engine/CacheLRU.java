package ore.forge.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

public class CacheLRU<K, E extends Sizeable> implements Cache<K, E> {
    private long maxSizeBytes;
    private long currentSizeBytes;
    private final Deque<K> elements;
    private final HashMap<K, E> lookup;

    public CacheLRU(long maxSizeBytes) {
        if (maxSizeBytes < 0) {
            throw new IllegalArgumentException("maxSizeBytes can't be negative");
        }
        this.maxSizeBytes = maxSizeBytes;
        this.currentSizeBytes = 0;
        this.elements = new ArrayDeque<>();
        this.lookup = new HashMap<>();
    }

    @Override
    public void put(K key, E value) {
        long size = value.sizeInBytes();
        if (size > maxSizeBytes) {
            throw new IllegalArgumentException(String.format("size %d is greater than %d", size, maxSizeBytes));
        }

        if (lookup.containsKey(key)) {
            var old = lookup.replace(key, value);
            currentSizeBytes -= old.sizeInBytes();
            old.dispose();
        } else {
            elements.addFirst(key);
            lookup.put(key, value);
        }

        if (size + currentSizeBytes > maxSizeBytes) {
            evict((size + currentSizeBytes) - maxSizeBytes);
        }
        currentSizeBytes += size;

    }

    @Override
    public E take(K key) {
        elements.remove(key);
        var toReturn = lookup.remove(key);
        if (toReturn != null) {
            this.currentSizeBytes -= toReturn.sizeInBytes();
//            Gdx.app.log("LRU Cache", "Cache Hit!");
        }
        return toReturn;
    }

    @Override
    public boolean contains(K key) {
        return lookup.containsKey(key);
    }

    @Override
    public int numElements() {
        return elements.size();
    }

    @Override
    public long totalSizeBytes() {
        return currentSizeBytes;
    }

    @Override
    public long maxSizeBytes() {
        return this.maxSizeBytes;
    }

    @Override
    public void setMaxSizeBytes(long maxSizeBytes) {
        if (maxSizeBytes < 0) {
           throw new IllegalArgumentException("maxSizeBytes can't be negative");
        }
        this.maxSizeBytes = maxSizeBytes;
        if (this.currentSizeBytes > this.maxSizeBytes) {
            long toFree = this.currentSizeBytes - this.maxSizeBytes;
            evict(toFree);
        }
    }

    private void evict(long toFree) {
        long freed = 0;
        while (freed < toFree) {
            E removed = lookup.remove(elements.removeLast());
            freed += removed.sizeInBytes();
            removed.dispose();
        }
//        Gdx.app.log("LRU Cache", "Freed: " + freed / Sizeable.MB + "MB");
        this.currentSizeBytes -= freed;
    }

}
