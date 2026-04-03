package ore.forge.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.PooledEngine;
import ore.forge.engine.components.PhysicsC;

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
