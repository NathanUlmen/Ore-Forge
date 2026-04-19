package ore.forge.game.temp;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import ore.forge.game.Tickable;
import ore.forge.game.UpdatableScriptC;

public class UpdatableSystem extends IteratingSystem {
    private static final Family FAMILY = Family.all(UpdatableScriptC.class).get();


    public UpdatableSystem() {
        super(FAMILY);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        UpdatableScriptC updatableScriptC = entity.getComponent(UpdatableScriptC.class);
        for (Tickable tickable : updatableScriptC.scripts) {
            tickable.update(deltaTime, null, entity);
        }
    }
}
