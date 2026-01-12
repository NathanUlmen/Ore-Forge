package ore.forge.Input3D;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.RayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseProxy;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import ore.forge.CollisionRules;
import ore.forge.GameContext;
import ore.forge.PhysicsWorld;

public abstract class InputState {
    protected final CameraController cameraController;
    protected final InputHandler inputHandler;
    protected GameContext context;

    public InputState(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.cameraController = inputHandler.cameraController();
    }

    public abstract void update(float delta);

    public RayResultCallback rayCastForItem() {
        btCollisionWorld dynamicsWorld = PhysicsWorld.instance().dynamicsWorld();

        Vector3 rayFrom = new Vector3(cameraController.getCamera().position);
        Vector3 rayTo = cameraController.getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY()).direction.cpy().scl(1000).add(rayFrom);

        RayResultCallback rayCallback = new ClosestRayResultCallback(rayFrom, rayTo) {
            @Override
            public boolean needsCollision(btBroadphaseProxy proxy) {
                return (proxy.getCollisionFilterGroup() & CollisionRules.combineBits(CollisionRules.ORE_PROCESSOR)) != 0;
            }
        };

        // Perform the ray test
        dynamicsWorld.rayTest(rayFrom, rayTo, rayCallback);

        return rayCallback;
    }


}
