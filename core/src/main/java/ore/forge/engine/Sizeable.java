package ore.forge.engine;

import com.badlogic.gdx.utils.Disposable;

public interface Sizeable extends Disposable {
    long KB = 1024;
    long MB = 1024 * 1024;
    long GB = 1024 * 1024 * 1024;

    long sizeInBytes();

}
