package ore.forge;

import com.badlogic.gdx.Gdx;
import ore.forge.EventSystem.EventManager;
import ore.forge.Player.Player;
import ore.forge.Strategies.Updatable;

import java.nio.Buffer;


public class GameContext {
    public static final GameContext INSTANCE = new GameContext();
    public final EntityManager entityManager;
    public final PreviewManager previewManager;
    public final EventManager eventManager;
    public final PhysicsWorld physicsWorld;
    public final CollisionManager collisionManager;
    public final StagedCollection<Updatable> updatables;
    public final Player player;

    public GameContext() {
        previewManager = new PreviewManager();
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
        for (final Updatable updatable : updatables) {
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

    public void flush() {
        entityManager.flush(this);
        previewManager.flush();

        updatables.flush();
    }

    public void save() {
        player.save();
        System.out.println("Saving...(Unimplemented as of Now)");
    }

    public void load() {
//        player.inventory.load();

    }

}
