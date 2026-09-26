package ore.forge.engine;

/**
 * @author Nathan Ulmen
 * A Handle is used to reference resources owned by other systems.
 * Uses a 64bit integer to do so. The top 32 bits store the index to the resource, the
 * bottom 32 bits store a version field. The version is used to make sure the handle is pointing to
 * the correct resource. This is Validated by the system that manages the resources.
 *
 */
public class Handle<E> {
    private long handle;

    public Handle(int index, int version) {
        this.handle = ((long) index << 32) | (version & 0xFFFF_FFFFL);
    }

    public Handle(Handle<E> handle) {
       this.handle  = handle.handle;
    }

    public boolean isValid() {
        return handle != 0;
    }

    public int version() {
        return (int) (handle);
    }

    public int index() {
        return (int) (handle >>> 32);
    }

    /**
     * Returns the handle identity for manager bookkeeping.
     * Callers should capture it before final-release invalidation.
     */
    public long identity() {
        return handle;
    }

    void invalidate() {
        this.handle &= (handle << 32);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(handle);
    }

    @Override
    public boolean equals(Object handle) {
        return handle instanceof Handle && this.handle == ((Handle) handle).handle;
    }

    public String toString() {
        return String.format("{version=%d, index=%d}", index(), version());
    }

}
