import com.badlogic.gdx.physics.box2d.World;
import ore.forge.CollisionManager;
import ore.forge.EntityInstance;
import ore.forge.EventSystem.EventManager;
import ore.forge.PhysicsWorld;
import ore.forge.Player.ItemInventory;
import ore.forge.Strategies.TimeUpdatable;
import ore.forge.UI.UI;

import javax.swing.*;
import java.util.Deque;
import java.util.List;

public class GameContext {
    public World world;
    public List<TimeUpdatable> updatable; //update
    public EventManager eventManager;
    public Renderer renderer;
    public PhysicsWorld physicsWorld;
    public CollisionManager collisionManager;
    public UI ui;
//    public Player player;
    public ItemInventory itemInventory;
    public Deque<EntityInstance> toAdd, toRemove;

    public GameContext() {

    }

    public void update(float delta) {
        //update our physics step
        physicsWorld.dynamicsWorld().stepSimulation(delta);

        for (TimeUpdatable e : updatable) {
            e.update(delta);
        }


        while (!toRemove.isEmpty()) {
            //remove entities from world and renderer
        }

        while (!toAdd.isEmpty()) {
            //add entities to world and renderer
        }

    }

    public void addEntity(EntityInstance entity) {
        toAdd.push(entity);
    }

    public void removeEntity(EntityInstance entity) {
        toRemove.push(entity);
    }

    public void save() {

    }

}
