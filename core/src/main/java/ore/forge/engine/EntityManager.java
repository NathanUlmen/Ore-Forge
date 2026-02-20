package ore.forge.engine;

import com.badlogic.ashley.core.Entity;
import ore.forge.game.GameContext;

import java.util.Iterator;

/**
 * Entity Manager tracks the lifetime of entities as they are added and removed from the world.
 * Systems can listen in to see when entities are removed or added and update themselves based on the entity in the event.
 * */
public class EntityManager implements Iterable<Entity> {
    private final StagedCollection<Entity> activeEntities;

    public EntityManager() {
        activeEntities = new StagedCollection<>();
    }

    public void stageAdd(Entity Entity) {
        activeEntities.stageAddition(Entity);
    }

    public void stageRemove(Entity Entity) {
        activeEntities.stageRemoval(Entity);
    }

    public void flush(GameContext ctx) {
        //Remove entities
        for (Entity e : activeEntities.toRemove()) {
            //Remove from collisionManager
//            ctx.collisionManager.removeAllPairsWith(e);

            //Remove from physics
//            PhysicsAdder.removeEntity(e, ctx.physicsWorld);

            //Remove all its updatables
//            for (Updatable updatable : e.updatables) {
//                ctx.updatables.stageRemoval(updatable);
//            }

            //event manager can log here!

//            e.dispose();
        }

        //Add entities
        for (Entity e : activeEntities.toAdd()) {
            //add to physics
//            PhysicsAdder.addEntity(e, ctx.physicsWorld);

            //add updatables
//            for (Updatable updatable : e.updatables) {
//                ctx.updatables.stageAddition(updatable);
//            }

        }

//        activeEntities.flush();
    }

    @Override
    public String toString() {
        return  "";
    }

    @Override
    public Iterator<Entity> iterator() {
        return null;
    }

//    public Iterator<Entity> iterator() {
//        return activeEntities.iterator();
//    }

}
