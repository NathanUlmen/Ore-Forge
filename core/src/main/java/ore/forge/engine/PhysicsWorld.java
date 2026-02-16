package ore.forge.engine;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Disposable;

public class PhysicsWorld implements Disposable {
    private static final PhysicsWorld instance = instance();
    private final btDiscreteDynamicsWorld dynamicsWorld;
    private final btDispatcher dispatcher;
    private final btBroadphaseInterface broadphase;
    private final btConstraintSolver solver;
    private final btCollisionConfiguration collisionConfig;
    private final DebugDrawer debugDrawer;

    private PhysicsWorld() {
        Bullet.init();
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcherMt(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btConstraintSolverPoolMt(Runtime.getRuntime().availableProcessors());
        dynamicsWorld = new btDiscreteDynamicsWorldMt(dispatcher, broadphase, (btConstraintSolverPoolMt) solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -9.81f, 0));

        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_DrawWireframe);


        dynamicsWorld.setDebugDrawer(debugDrawer);
    }

    public void drawDebug(Camera camera) {
        debugDrawer.begin(camera);
        dynamicsWorld.debugDrawWorld();
        debugDrawer.end();
    }

    public static PhysicsWorld instance() {
        return instance != null ? instance : new PhysicsWorld();
    }

    public btDynamicsWorld dynamicsWorld() {
        return dynamicsWorld;
    }


    @Override
    public void dispose() {
        dynamicsWorld.dispose();
        collisionConfig.dispose();
        debugDrawer.dispose();
        solver.dispose();
        broadphase.dispose();
    }
}
