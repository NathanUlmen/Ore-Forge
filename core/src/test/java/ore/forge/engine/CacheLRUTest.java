package ore.forge.engine;

import ore.forge.engine.resources.AssetID;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CacheLRUTest {
    @Test
    void putAndTakeTrackEntriesAndSize() {
        CacheLRU cache = new CacheLRU(10);
        AssetID key = key();
        TestSizeable value = value(4);

        cache.put(key, value);

        assertTrue(cache.contains(key));
        assertEquals(1, cache.numElements());
        assertEquals(4, cache.totalSizeBytes());
        assertSame(value, cache.take(key));
        assertFalse(cache.contains(key));
        assertEquals(0, cache.totalSizeBytes());
    }

    @Test
    void takePromotesEntryAndEvictsLeastRecentlyUsedValue() {
        CacheLRU cache = new CacheLRU(8);
        AssetID oldest = key();
        AssetID newest = key();
        AssetID replacement = key();
        TestSizeable oldestValue = value(4);
        TestSizeable newestValue = value(4);

        cache.put(oldest, oldestValue);
        cache.put(newest, newestValue);
        assertSame(oldestValue, cache.take(oldest));
        cache.put(replacement, value(4));

        assertFalse(cache.contains(oldest));
        assertTrue(cache.contains(newest));
        assertFalse(newestValue.disposed);
        assertEquals(8, cache.totalSizeBytes());
    }

    @Test
    void replacingKeyReplacesValueWithoutDuplicatingEntry() {
        CacheLRU cache = new CacheLRU(10);
        AssetID key = key();
        TestSizeable oldValue = value(4);
        TestSizeable newValue = value(6);

        cache.put(key, oldValue);
        cache.put(key, newValue);

        assertEquals(1, cache.numElements());
        assertEquals(6, cache.totalSizeBytes());
        assertSame(newValue, cache.take(key));
        assertTrue(oldValue.disposed);
    }

    @Test
    void shrinkingCapacityEvictsUntilWithinLimit() {
        CacheLRU cache = new CacheLRU(12);
        AssetID first = key();
        AssetID second = key();
        AssetID third = key();
        TestSizeable firstValue = value(4);
        TestSizeable secondValue = value(4);
        TestSizeable thirdValue = value(4);

        cache.put(first, firstValue);
        cache.put(second, secondValue);
        cache.put(third, thirdValue);
        cache.setMaxSizeBytes(5);

        assertEquals(4, cache.totalSizeBytes());
        assertEquals(1, cache.numElements());
        assertTrue(firstValue.disposed);
        assertTrue(secondValue.disposed);
        assertFalse(thirdValue.disposed);
        assertTrue(cache.contains(third));
    }

    @Test
    void rejectsNegativeCapacityAndOversizedValues() {
        assertThrows(IllegalArgumentException.class, () -> new CacheLRU(-1));

        CacheLRU cache = new CacheLRU(4);
        assertThrows(IllegalArgumentException.class, () -> cache.put(key(), value(5)));
        assertEquals(0, cache.numElements());
        assertEquals(0, cache.totalSizeBytes());
    }

    private static AssetID key() {
        return new AssetID(UUID.randomUUID());
    }

    private static TestSizeable value(long size) {
        return new TestSizeable(size);
    }

    private static final class TestSizeable implements Sizeable {
        private final long size;
        private boolean disposed;

        private TestSizeable(long size) {
            this.size = size;
        }

        @Override
        public long sizeInBytes() {
            return size;
        }

        @Override
        public void dispose() {
            disposed = true;
        }
    }
}
