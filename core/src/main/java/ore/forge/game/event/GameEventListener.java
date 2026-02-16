package ore.forge.game.event;


public interface GameEventListener<E> {
    void handle(E event);

    Class<?> getEventType();

}
