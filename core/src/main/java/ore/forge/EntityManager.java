package ore.forge;

import ore.forge.Strategies.Updatable;

import java.util.Iterator;

public class EntityManager implements Iterable<EntityInstance> {
    private final StagedCollection<EntityInstance> entities;

    public EntityManager() {
        entities = new StagedCollection<>();
    }

    public void stageAdd(EntityInstance entityInstance) {
        entities.stageAddition(entityInstance);
    }

    public void stageRemove(EntityInstance entityInstance) {
        entities.stageRemoval(entityInstance);
    }

    public void flush(GameContext ctx) {
        //Remove entities
        for (EntityInstance e : entities.toRemove()) {
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
        for (EntityInstance e : entities.toAdd()) {
            //add to physics
            e.addToWorld(ctx.physicsWorld.dynamicsWorld());

            //add updatables
            for (Updatable updatable : e.updatables) {
                ctx.updatables.stageAddition(updatable);
            }

        }


        entities.flush();
    }

    @Override
    public Iterator<EntityInstance> iterator() {
        return entities.iterator();
    }
}
