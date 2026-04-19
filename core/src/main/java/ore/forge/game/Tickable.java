package ore.forge.game;

import com.badlogic.ashley.core.Entity;

public interface Tickable {
    public void update(float delta, GameContext2 gameContext, Entity entity);
}
