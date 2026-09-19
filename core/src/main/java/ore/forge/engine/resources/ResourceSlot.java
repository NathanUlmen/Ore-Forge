package ore.forge.engine.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;

public class ResourceSlot<E extends Disposable> implements Disposable {

    public enum LoadState {
        REQUESTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    private static final String LOG_STRING = ResourceSlot.class.getName();
    public E current;
    private final E placeHolder;
    private LoadState state;
   
    public ResourceSlot(E placeHolder, LoadState state) {
        this.placeHolder = placeHolder;
        this.state = state;
        current = null;
    }

    public E getData() {
        return current == null ? placeHolder : current;
    }

    public void resolve(E primaryResource) {
        if (current == null) { 
            this.current = primaryResource;
        } else {
            Gdx.app.log(LOG_STRING, "Attempted to set resouce that was already set." + this.toString());
        }
    }

    public void setLoadState(LoadState newState) {
        this.state = newState;
    }

    public LoadState getLoadState() {
        return state;
    }
    public boolean isResolved() { 
        return current != null;
    }

    @Override
    public void dispose() {
        if (current != null) {
            current.dispose();
        }
        if (placeHolder != null) {
            placeHolder.dispose();
        }
    }

    public String toString()  {
        return current.toString() + placeHolder.toString();
    }

}
