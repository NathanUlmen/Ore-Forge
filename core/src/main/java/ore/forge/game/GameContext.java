package ore.forge.game;

import ore.forge.game.event.EventManager;
import ore.forge.engine.PhysicsBody;
import ore.forge.game.player.Player;
import ore.forge.engine.PreviewManager;
import ore.forge.engine.EntityInstance;
import ore.forge.engine.EntityManager;
import ore.forge.engine.PhysicsWorld;
import ore.forge.engine.StagedCollection;


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
        for (EntityInstance instance : entityManager) {
            instance.snapshotPreview();
        }

        physicsWorld.dynamicsWorld().stepSimulation(1/60f, 2, 1/60f);

        collisionManager.updateTouchingEntities(delta);

        //update transforms
        for (EntityInstance entity : entityManager) {
            for (PhysicsBody body : entity.physicsComponent.getBodies()) {
                body.readFromBullet();
            }
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
