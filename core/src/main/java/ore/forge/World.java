package ore.forge;

import ore.forge.Strategies.Updatable;

public class World {
    private Foo<EntityInstance> entities;
    private Foo<Updatable> updatables;

    public World() {
        entities = new Foo<>();
        updatables = new Foo<>();
    }

    public void update(float deltaT, GameState state) {
        for (Updatable updatable : updatables.getElements()) {
            updatable.update(deltaT, state);
        }

        updatables.updateLists();

        entities.updateLists();
    }

}
