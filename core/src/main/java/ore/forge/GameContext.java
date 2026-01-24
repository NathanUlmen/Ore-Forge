package ore.forge;

import com.badlogic.gdx.assets.AssetManager;
import ore.forge.EventSystem.EventManager;
import ore.forge.Items.ItemDefinition;
import ore.forge.Player.Player;
import ore.forge.Strategies.Updatable;

import java.util.List;

public class GameContext {
    public static final GameContext INSTANCE = new GameContext();
    public final AssetManager assetManager;
    public final EntityManager entityManager;
    public final EventManager eventManager;
    public final PhysicsWorld physicsWorld;
    public final CollisionManager collisionManager;
    public final StagedCollection<Updatable> updatables;
    public final Player player;

    public GameContext() {
        assetManager = new AssetManager();
        entityManager = new EntityManager();
        eventManager = new EventManager();
        physicsWorld = PhysicsWorld.instance();
        collisionManager = new CollisionManager(this);
        player = new Player();
        updatables = new StagedCollection<>();
    }

    public void update(float delta) {
        //update our physics step
        physicsWorld.dynamicsWorld().stepSimulation(delta, 0);

        collisionManager.updateTouchingEntities(delta);

        //update transforms
        for (EntityInstance entity : entityManager) {
            entity.syncFromPhysics();
        }

        //update our time based updates
        for(final Updatable updatable : updatables) {
            updatable.update(delta, this);
        }

        //update our event manager here if we decide to rework it

        //update our entity lists
        flush();
    }

    public void addUpdatable(Updatable updatable) {
        this.updatables.stageAddition(updatable);
    }

    public void removeUpdatable(Updatable updatable) {
        this.updatables.stageRemoval(updatable);
    }

    /**
     *
     * */
    public void flush() {
        entityManager.flush(this);

        updatables.flush();
    }

    public void save() {

    }

    public void load() {
//        player.inventory.load();

    }

}
