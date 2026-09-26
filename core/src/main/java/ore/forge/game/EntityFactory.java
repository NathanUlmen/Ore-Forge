package ore.forge.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;

/*
*
*
* */
public class EntityFactory extends EntitySystem {
    private final Engine engine;

    public EntityFactory(PooledEngine engine) {
        this.engine = engine;
    }

}
