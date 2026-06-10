package ore.forge.engine;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import ore.forge.engine.definitions.Asset;

/**
 * @author Nathan Ulmen
 * A Handle is used to reference resources owned by other systems.
 * Uses a 64bit integer to do so. The top 32 bits store the index to the resource, the
 * bottom 32 bits store a version field. The version is used to make sure the handle is pointing to
 * the correct resource. This is Validated by the system that manages the resources.
 *
 */
public class Handle<E> implements Cloneable {
    private final long handle;

    public Handle(int index, int version) {
        this.handle = ((long) index << 32) | (version & 0xFFFF_FFFFL);
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


}
