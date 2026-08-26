package ore.forge.engine;

import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;

/** @author Nathan Ulmen
* Listents to events 
*/
public class ComponentListener implements EntityListener {
    private final Consumer<Entity> onEntityAdded;
    private final Consumer<Entity> onEntityRemoved;

    public ComponentListener(Consumer<Entity> onAdded, Consumer<Entity> onRemoved) {
        this.onEntityAdded = onAdded;
        this.onEntityRemoved = onRemoved;
    }

    @Override
    public void entityAdded(Entity entity) {
        onEntityAdded.accept(entity);
    }

    @Override
    public void entityRemoved(Entity entity) {
        onEntityRemoved.accept(entity);
    }
    
}
