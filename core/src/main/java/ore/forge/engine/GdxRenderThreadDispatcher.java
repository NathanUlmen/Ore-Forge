package ore.forge.engine;

import com.badlogic.gdx.Gdx;

public class GdxRenderThreadDispatcher implements Dispatcher {

    @Override
    public void post(Runnable runnable) {
        Gdx.app.postRunnable(runnable);
    }
}
