package ore.forge;

import ore.forge.EventSystem.EventManager;
import ore.forge.UI.UI;

public class GameState {
    public EventManager eventManager;
    public PhysicsWorld physicsWorld;
    public CollisionManager collisionManager;
    public World world;
    public UI ui;

    public GameState() {
        eventManager = new EventManager();
        world = new  World();
        collisionManager = new CollisionManager(this);
    }

    public void update(float delta) {
        //Step world forward
        physicsWorld.dynamicsWorld().stepSimulation(delta);

        //
        collisionManager.updateTouchingEntities();

        //Update
        world.update(delta, this);

        ui.act(delta);
        ui.draw();
    }

    public void addToWorld(EntityInstance entity) {

    }

    public void removeFromWorld(EntityInstance entity) {

    }

}
