package ore.forge.engine;

public interface Cache<K, E extends Sizeable > {

    void put(K key, Sizeable value);

    Sizeable get(K key);

    boolean contains(K key);

    int numElements();

    long totalSizeBytes();

    long maxSizeBytes();

    void setMaxSizeBytes(long maxSizeBytes);

}
