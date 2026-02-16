package ore.forge.engine;

import ore.forge.game.GameContext;
import ore.forge.game.Updatable;

import java.util.Iterator;

/**
 * Entity Manager tracks the lifetime of entities as they are added and removed from the world.
 * Systems can listen in to see when entities are removed or added and update themselves based on the entity in the event.
 * */
public class EntityManager implements Iterable<EntityInstance> {
    private final StagedCollection<EntityInstance> activeEntities;

    public EntityManager() {
        activeEntities = new StagedCollection<>();
    }

    public void stageAdd(EntityInstance entityInstance) {
        activeEntities.stageAddition(entityInstance);
    }

    public void stageRemove(EntityInstance entityInstance) {
        activeEntities.stageRemoval(entityInstance);
    }

    public void flush(GameContext ctx) {
        //Remove entities
        for (EntityInstance e : activeEntities.toRemove()) {
            //Remove from collisionManager
            ctx.collisionManager.removeAllPairsWith(e);

            //Remove from physics
            e.removeFromWorld(ctx.physicsWorld.dynamicsWorld());

            //Remove all its updatables
            for (Updatable updatable : e.updatables) {
                ctx.updatables.stageRemoval(updatable);
            }

            //event manager can log here!

            e.dispose();
        }

        //Add entities
        for (EntityInstance e : activeEntities.toAdd()) {
            //add to physics
            e.addToWorld(ctx.physicsWorld.dynamicsWorld());

            //add updatables
            for (Updatable updatable : e.updatables) {
                ctx.updatables.stageAddition(updatable);
            }

        }

        activeEntities.flush();
    }

    @Override
    public String toString() {
        return  activeEntities.toString();
    }

    @Override
    public Iterator<EntityInstance> iterator() {
        return activeEntities.iterator();
    }

}
