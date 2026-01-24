package ore.forge;

import ore.forge.Strategies.Updatable;

import java.util.Iterator;

public class EntityManager implements Iterable<EntityInstance> {
    private final StagedCollection<EntityInstance> activeEntities;
    private final StagedCollection<EntityInstance> previewEntities;

    public EntityManager() {
        activeEntities = new StagedCollection<>();
        previewEntities = new StagedCollection<>();
    }

    public void stageAdd(EntityInstance entityInstance) {
        activeEntities.stageAddition(entityInstance);
    }

    public void stageRemove(EntityInstance entityInstance) {
        activeEntities.stageRemoval(entityInstance);
    }

    public void addPreviewEntity(EntityInstance entityInstance) { previewEntities.stageAddition(entityInstance); }

    public void removePreviewEntity(EntityInstance entityInstance) { previewEntities.stageRemoval(entityInstance); }

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
        previewEntities.flush();
    }

    @Override
    public Iterator<EntityInstance> iterator() {
        return activeEntities.iterator();
    }

    public Iterable<EntityInstance> allEntities() {
        return () -> new Iterator<>() {
            private final Iterator<EntityInstance> activeIt = activeEntities.iterator();
            private final Iterator<EntityInstance> previewIt = previewEntities.iterator();

            @Override
            public boolean hasNext() {
                return activeIt.hasNext() || previewIt.hasNext();
            }

            @Override
            public EntityInstance next() {
                return activeIt.hasNext() ? activeIt.next() : previewIt.next();
            }
        };
    }

}
