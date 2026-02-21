package ore.forge.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;
import ore.forge.engine.PhysicsWorld;
import ore.forge.engine.systems.PostPhysicsTransformSyncSystem;
import ore.forge.engine.systems.PrePhysicsTransformSyncSystem;
import ore.forge.engine.systems.RenderPrepSystem;
import ore.forge.game.collisions.PhysicsCallbackResolverSystem;
import ore.forge.game.collisions.CollisionManager;
import ore.forge.game.player.Player;

public class GameContext2 {
    public static final float FIXED_TIME_STEP = 1/60f;
    public static final int MAX_SUBSTEPS = 3;
    public final Engine engine;
    public final Player player;
    public final PhysicsWorld physicsWorld;
    public final CollisionManager collisionManager;
    private final PhysicsCallbackResolverSystem callbackResolver;

    public GameContext2() {
        engine = new PooledEngine();
        player = new Player();
        physicsWorld = PhysicsWorld.instance();
        collisionManager = new CollisionManager();
        callbackResolver = new PhysicsCallbackResolverSystem(this, collisionManager);
        engine.addSystem(callbackResolver);
    }

    public void update(float delta) {

        //pre sync transforms
        var preTickSync = engine.getSystem(PrePhysicsTransformSyncSystem.class);
        preTickSync.update(delta);

        //step Physics
        physicsWorld.dynamicsWorld().stepSimulation(delta, MAX_SUBSTEPS, FIXED_TIME_STEP);

        //update contacts
        collisionManager.update(delta);

        //resolve callbacks
        var physicsCallbackResolver = engine.getSystem(PhysicsCallbackResolverSystem.class);
        physicsCallbackResolver.update(delta);

        //sync after physics
        var postTickSync = engine.getSystem(PostPhysicsTransformSyncSystem.class);
        postTickSync.update(delta);

        //prepareRender
        var prepareRender =  engine.getSystem(RenderPrepSystem.class);
        prepareRender.update(delta);

    }

}
