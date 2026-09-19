package ore.forge.engine;

public interface Dispatcher {

    void post(Runnable runnable);
    
}
